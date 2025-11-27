package com.example.backendgym.service;

import com.example.backendgym.domain.*;
import com.example.backendgym.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;

@Service
public class PagoService {

    private final CarritoRepository carritoRepository;
    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final SuscripcionUsuarioRepository suscripcionUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final MailService mailService;

    // === Mercado Pago props ===
    @Value("${mp.access.token:}")
    private String mpAccessToken;

    @Value("${mp.back.success:http://localhost:4200/gracias}")
    private String mpBackSuccess;

    @Value("${mp.back.failure:http://localhost:4200/pagos}")
    private String mpBackFailure;

    @Value("${mp.back.pending:http://localhost:4200/pagos}")
    private String mpBackPending;

    @Value("${mp.notification.url:http://localhost:8080/api/pagos/mercadopago/webhook}")
    private String mpNotificationUrl;

    // === NUEVO: base del Front para redirección final (Home) ===
    @Value("${app.front.base-url:http://localhost:4200/}")
    private String frontBaseUrl;

    public PagoService(CarritoRepository carritoRepository,
                       PedidoRepository pedidoRepository,
                       PagoRepository pagoRepository,
                       SuscripcionUsuarioRepository suscripcionUsuarioRepository,
                       UsuarioRepository usuarioRepository,
                       ProductoRepository productoRepository,
                       MailService mailService) {
        this.carritoRepository = carritoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
        this.suscripcionUsuarioRepository = suscripcionUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.mailService = mailService;
    }

    private void incrementarPopularidadDePedido(Pedido pedido) {
        if (pedido == null || pedido.getDetalles() == null) return;
        for (DetallePedido d : pedido.getDetalles()) {
            Producto prod = d.getProducto();
            if (prod == null) continue;
            try {
                Long pid = prod.getId();
                if (pid != null) {
                    Producto dbProd = productoRepository.findById(pid).orElse(null);
                    if (dbProd != null) {
                        Integer pop = dbProd.getPopularidad();
                        if (pop == null) pop = 0;
                        int cantidad = d.getCantidad() != null ? d.getCantidad() : 1;
                        dbProd.setPopularidad(pop + cantidad);
                        productoRepository.save(dbProd);
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public byte[] generarPdfComprobantePedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Pago pago = pagoRepository.findAll().stream()
                .filter(pg -> pg.getPedido().getId().equals(pedidoId))
                .findFirst().orElse(null);

        Usuario usuario = pedido.getUsuario();
        String correo = usuario != null ? usuario.getEmail() : null;
        String nombre = (usuario != null && usuario.getNombre() != null) ? usuario.getNombre() : "Cliente";
        BigDecimal total = pedido.getTotal();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String fecha = (pago != null && pago.getFechaPago() != null
                ? pago.getFechaPago()
                : LocalDateTime.now()).format(fmt);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document();
            PdfWriter.getInstance(doc, baos);
            doc.open();

            try {
                Image logo = Image.getInstance("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRooqX_tEjlq63fL2GQIpdJ50tuKZ7qT-qJ8A&s");
                logo.scaleToFit(80, 80);
                logo.setAlignment(Image.ALIGN_RIGHT);
                doc.add(logo);
            } catch (Exception ignored) {}

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

            doc.add(new Paragraph("Comprobante de pago", titleFont));
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("Fecha: " + fecha, normalFont));
            doc.add(new Paragraph("Pedido: #" + pedido.getId(), normalFont));
            doc.add(new Paragraph("Cliente: " + nombre, normalFont));
            if (correo != null) doc.add(new Paragraph("Correo: " + correo, normalFont));
            doc.add(new Paragraph("Metodo de pago: " + (pedido.getMetodoPago() != null ? pedido.getMetodoPago() : "N/A"), normalFont));
            doc.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{5f, 1.5f, 2.5f});

            PdfPCell c1 = new PdfPCell(new Paragraph("Producto", boldFont));
            PdfPCell c2 = new PdfPCell(new Paragraph("Cant.", boldFont));
            PdfPCell c3 = new PdfPCell(new Paragraph("Subtotal", boldFont));
            Color headerBg = new Color(239, 246, 255);
            Color headerText = new Color(37, 99, 235);
            c1.setBackgroundColor(headerBg);
            c2.setBackgroundColor(headerBg);
            c3.setBackgroundColor(headerBg);
            c1.getPhrase().getFont().setColor(headerText);
            c2.getPhrase().getFont().setColor(headerText);
            c3.getPhrase().getFont().setColor(headerText);
            table.addCell(c1);
            table.addCell(c2);
            table.addCell(c3);

            if (pedido.getDetalles() != null) {
                for (DetallePedido d : pedido.getDetalles()) {
                    String prod = d.getProducto() != null ? d.getProducto().getNombre() : "Producto";
                    table.addCell(new Paragraph(prod, normalFont));
                    table.addCell(new Paragraph(String.valueOf(d.getCantidad()), normalFont));
                    table.addCell(new Paragraph("S/ " + d.getSubtotal(), normalFont));
                }
            }

            PdfPCell totalLabel = new PdfPCell(new Paragraph("Total", boldFont));
            totalLabel.setColspan(2);
            table.addCell(totalLabel);
            table.addCell(new Paragraph("S/ " + total, boldFont));

            doc.add(table);
            doc.add(new Paragraph("\n"));
            Font small = FontFactory.getFont(FontFactory.HELVETICA, 9);
            doc.add(new Paragraph("Si tienes alguna duda o problema con tu pago, puedes escribirnos en:", small));
            Font linkFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.BLUE);
            doc.add(new Paragraph("http://localhost:4200/consultas", linkFont));
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar el PDF del comprobante");
        }
    }

    /**
     * Después del return de MP, redirigimos SIEMPRE al Home del front.
     * Si no fue approved, agregamos un query param ?resultado=<status> por si quieres mostrar un toast.
     */
    public String buildReturnUrl(Map<String, String> params) {
        String status = null;
        if (params != null) {
            status = params.get("status");
            if (status == null) status = params.get("collection_status");
            if (status == null) status = params.get("resultado");
        }
        // Normalizamos el frontBaseUrl para que termine con "/"
        String base = frontBaseUrl == null || frontBaseUrl.isBlank() ? "http://localhost:4200/" : frontBaseUrl;
        if (!base.endsWith("/")) base = base + "/";

        if (status != null && "approved".equalsIgnoreCase(status)) {
            // Éxito → Home sin parámetros
            return base;
        }
        // Cualquier otro estado → Home con query param opcional
        if (status != null && !status.isBlank()) {
            return base + "?resultado=" + status;
        }
        return base;
    }

    @Transactional
    public Map<String, Object> iniciarPedidoDesdeCarrito(Long usuarioId, Pedido.MetodoPago metodoPago) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        List<Carrito> items = carritoRepository.findByUsuarioOrderByFechaAgregadoDesc(usuario);
        if (items.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        BigDecimal subtotal = items.stream().map(Carrito::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int descuento = obtenerDescuento(usuarioId);
        BigDecimal descuentoMonto = subtotal.multiply(new BigDecimal(descuento)).divide(new BigDecimal(100));
        BigDecimal total = subtotal.subtract(descuentoMonto);

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setMetodoPago(metodoPago);
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
        pedido.setFechaPedido(LocalDateTime.now());
        for (Carrito it : items) {
            DetallePedido det = new DetallePedido();
            det.setProducto(it.getProducto());
            det.setCantidad(it.getCantidad());
            det.setPrecioUnitario(it.getPrecioUnitario());
            det.calcularSubtotal();
            pedido.agregarDetalle(det);
        }
        pedido.calcularTotal();
        if (descuento > 0) pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodoPago(metodoPago);
        pago.setMonto(pedido.getTotal());
        pago.setEstado(Pago.EstadoPago.PENDIENTE);
        pago.setFechaPago(LocalDateTime.now());
        pago = pagoRepository.save(pago);

        Map<String, Object> res = new HashMap<>();
        res.put("pedidoId", pedido.getId());
        res.put("monto", pedido.getTotal());
        res.put("moneda", "PEN");
        res.put("pagoId", pago.getId());
        res.put("estadoPago", pago.getEstado());
        return res;
    }

    @Transactional
    public Map<String, Object> confirmarPedidoAdmin(Long pedidoId, String referenciaPago) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        // 1) Validar stock antes de confirmar
        boolean stockOk = true;
        if (pedido.getDetalles() != null) {
            for (DetallePedido d : pedido.getDetalles()) {
                Producto prod = d.getProducto();
                if (prod == null) continue;
                Long pid = prod.getId();
                if (pid == null) continue;
                Producto dbProd = productoRepository.findById(pid).orElse(null);
                if (dbProd == null) continue;
                Integer stock = dbProd.getStock();
                int cant = d.getCantidad() != null ? d.getCantidad() : 0;
                if (cant <= 0) continue;
                if (stock == null || stock < cant) {
                    stockOk = false;
                    break;
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        if (!stockOk) {
            pedido.setEstado(Pedido.EstadoPedido.REVISION);
            pedidoRepository.save(pedido);
            res.put("pedidoId", pedido.getId());
            res.put("estadoPedido", pedido.getEstado());
            res.put("mensaje", "No hay stock suficiente para uno o más productos. El pedido se marcó en REVISIÓN.");
            return res;
        }

        // 2) Confirmar pago y pedido
        pedido.setEstado(Pedido.EstadoPedido.CONFIRMADO);
        pedido.setFechaConfirmacion(LocalDateTime.now());
        pedidoRepository.save(pedido);
        Pago pago = pagoRepository.findAll().stream().filter(pg -> pg.getPedido().getId().equals(pedidoId)).findFirst().orElse(null);
        if (pago != null) {
            pago.setEstado(Pago.EstadoPago.CONFIRMADO);
            if (referenciaPago != null) pago.setReferenciaPago(referenciaPago);
            pago.setFechaPago(LocalDateTime.now());
            pagoRepository.save(pago);
        }

        // 3) Descontar stock ahora que sabemos que alcanza
        if (pedido.getDetalles() != null) {
            for (DetallePedido d : pedido.getDetalles()) {
                Producto prod = d.getProducto();
                if (prod == null) continue;
                try {
                    Long pid = prod.getId();
                    if (pid == null) continue;
                    Producto dbProd = productoRepository.findById(pid).orElse(null);
                    if (dbProd == null) continue;
                    Integer stock = dbProd.getStock();
                    int cant = d.getCantidad() != null ? d.getCantidad() : 0;
                    if (cant <= 0) continue;
                    if (stock == null || stock < cant) {
                        // Esta condición no debería ocurrir porque ya validamos, pero se deja como guardia
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para confirmar el pedido");
                    }
                    dbProd.setStock(stock - cant);
                    productoRepository.save(dbProd);
                } catch (ResponseStatusException e) {
                    throw e;
                } catch (Exception ignored) {}
            }
        }

        // 4) Popularidad
        incrementarPopularidadDePedido(pedido);

        res.put("pedidoId", pedido.getId());
        res.put("estadoPedido", pedido.getEstado());
        if (pago != null) {
            res.put("pagoId", pago.getId());
            res.put("estadoPago", pago.getEstado());
        }
        return res;
    }

    @Transactional
    public Map<String, Object> confirmarPedidoUsuario(Long pedidoId, String referenciaPago) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Pago pago = pagoRepository.findAll().stream().filter(pg -> pg.getPedido().getId().equals(pedidoId)).findFirst().orElse(null);
        if (pago == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        boolean stockOk = true;
        if (pedido.getDetalles() != null) {
            for (DetallePedido d : pedido.getDetalles()) {
                Producto prod = d.getProducto();
                if (prod == null) continue;
                Long pid = prod.getId();
                if (pid == null) continue;
                Producto dbProd = productoRepository.findById(pid).orElse(null);
                if (dbProd == null) continue;
                Integer stock = dbProd.getStock();
                int cant = d.getCantidad() != null ? d.getCantidad() : 0;
                if (cant <= 0) continue;
                if (stock == null || stock < cant) {
                    stockOk = false;
                    break;
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        if (!stockOk) {
            pedido.setEstado(Pedido.EstadoPedido.REVISION);
            pedidoRepository.save(pedido);
            res.put("pedidoId", pedido.getId());
            res.put("estadoPedido", pedido.getEstado());
            res.put("mensaje", "No hay stock suficiente para uno o más productos. El pedido se marcó en REVISIÓN.");
            return res;
        }

        pago.setEstado(Pago.EstadoPago.CONFIRMADO);
        if (referenciaPago != null) pago.setReferenciaPago(referenciaPago);
        pago.setFechaPago(LocalDateTime.now());
        pagoRepository.save(pago);

        pedido.setEstado(Pedido.EstadoPedido.CONFIRMADO);
        pedido.setFechaConfirmacion(LocalDateTime.now());
        pedidoRepository.save(pedido);

        if (pedido.getDetalles() != null) {
            for (DetallePedido d : pedido.getDetalles()) {
                Producto prod = d.getProducto();
                if (prod == null) continue;
                try {
                    Long pid = prod.getId();
                    if (pid == null) continue;
                    Producto dbProd = productoRepository.findById(pid).orElse(null);
                    if (dbProd == null) continue;
                    Integer stock = dbProd.getStock();
                    int cant = d.getCantidad() != null ? d.getCantidad() : 0;
                    if (cant <= 0) continue;
                    if (stock == null || stock < cant) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para confirmar el pedido");
                    }
                    dbProd.setStock(stock - cant);
                    productoRepository.save(dbProd);
                } catch (ResponseStatusException e) {
                    throw e;
                } catch (Exception ignored) {}
            }
        }

        Usuario usuario = pedido.getUsuario();
        List<Carrito> items = carritoRepository.findByUsuarioOrderByFechaAgregadoDesc(usuario);
        for (Carrito item : items) {
            carritoRepository.delete(item);
        }

        res.put("pedidoId", pedido.getId());
        res.put("estadoPedido", pedido.getEstado());
        res.put("pagoId", pago.getId());
        res.put("estadoPago", pago.getEstado());
        return res;
    }

    @Transactional
    public Map<String, Object> cancelarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        pedido.setEstado(Pedido.EstadoPedido.CANCELADO);
        pedido.setFechaConfirmacion(LocalDateTime.now());
        pedidoRepository.save(pedido);
        Pago pago = pagoRepository.findAll().stream().filter(pg -> pg.getPedido().getId().equals(pedidoId)).findFirst().orElse(null);
        if (pago != null) {
            pago.setEstado(Pago.EstadoPago.CANCELADO);
            pago.setFechaPago(LocalDateTime.now());
            pagoRepository.save(pago);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("pedidoId", pedido.getId());
        res.put("estadoPedido", pedido.getEstado());
        if (pago != null) {
            res.put("pagoId", pago.getId());
            res.put("estadoPago", pago.getEstado());
        }
        return res;
    }

    public ResponseEntity<Map<String, Object>> crearPreferencia(Long usuarioId) {
        if (mpAccessToken == null || mpAccessToken.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "MP_TOKEN_MISSING");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
        Long uid = usuarioId;
        Usuario usuario = new Usuario();
        usuario.setId(uid);
        List<Carrito> items = carritoRepository.findByUsuario_IdOrderByFechaAgregadoDesc(uid);
        if (items.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "CARRITO_VACIO");
            err.put("usuarioId", uid);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        BigDecimal subtotal = items.stream().map(Carrito::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int descuento = obtenerDescuento(uid);
        BigDecimal descuentoMonto = subtotal.multiply(new BigDecimal(descuento)).divide(new BigDecimal(100));
        BigDecimal total = subtotal.subtract(descuentoMonto);

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setMetodoPago(Pedido.MetodoPago.MERCADO_PAGO);
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
        pedido.setFechaPedido(LocalDateTime.now());
        for (Carrito it : items) {
            DetallePedido det = new DetallePedido();
            det.setProducto(it.getProducto());
            det.setCantidad(it.getCantidad());
            det.setPrecioUnitario(it.getPrecioUnitario());
            det.calcularSubtotal();
            pedido.agregarDetalle(det);
        }
        pedido.calcularTotal();
        if (descuento > 0) pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodoPago(Pedido.MetodoPago.MERCADO_PAGO);
        pago.setMonto(pedido.getTotal());
        pago.setEstado(Pago.EstadoPago.PENDIENTE);
        pago.setFechaPago(LocalDateTime.now());
        pago = pagoRepository.save(pago);

        Map<String, Object> body = new HashMap<>();
        Map<String, Object> item = new HashMap<>();
        item.put("title", "Compra AngelsGym #" + pedido.getId());
        item.put("quantity", 1);
        item.put("currency_id", "PEN");
        item.put("unit_price", pedido.getTotal());
        body.put("items", List.of(item));

        String backSuccess = (mpBackSuccess != null && !mpBackSuccess.isBlank()) ? mpBackSuccess : "http://localhost:4200/";
        String backFailure = (mpBackFailure != null && !mpBackFailure.isBlank()) ? mpBackFailure : "http://localhost:4200/";
        String backPending = (mpBackPending != null && !mpBackPending.isBlank()) ? mpBackPending : "http://localhost:4200/";

        Map<String, Object> backUrls = new HashMap<>();
        backUrls.put("success", backSuccess);
        backUrls.put("failure", backFailure);
        backUrls.put("pending", backPending);
        body.put("back_urls", backUrls);
        body.put("auto_return", "approved");
        body.put("binary_mode", true);
        body.put("notification_url", mpNotificationUrl);
        body.put("external_reference", String.valueOf(pedido.getId()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mpAccessToken);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        RestTemplate rest = new RestTemplate();
        Map response;
        try {
            response = rest.postForObject("https://api.mercadopago.com/checkout/preferences", entity, Map.class);
        } catch (HttpClientErrorException e) {
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", e.getStatusCode().value());
            errorDetails.put("errorMP", e.getResponseBodyAsString());
            errorDetails.put("message", "Error al crear preferencia en Mercado Pago. Revisar errorMP para detalles.");
            errorDetails.put("back_urls_sent", backUrls);
            errorDetails.put("auto_return_sent", "approved");
            return ResponseEntity.status(e.getStatusCode()).body(errorDetails);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "Error al crear preferencia en Mercado Pago");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("pedidoId", pedido.getId());
        res.put("pagoId", pago.getId());
        if (response != null) {
            res.put("preferenceId", response.get("id"));
            res.put("init_point", response.get("init_point"));
            res.put("sandbox_init_point", response.get("sandbox_init_point"));
        }
        return ResponseEntity.ok(res);
    }

    public ResponseEntity<Map<String, Object>> crearPreferenciaSuscripcion(Long suscripcionId) {
        if (mpAccessToken == null || mpAccessToken.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "MP_TOKEN_MISSING");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
        SuscripcionUsuario su = suscripcionUsuarioRepository.findById(suscripcionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Suscripción no encontrada"));
        BigDecimal monto = su.getMonto();
        if (monto == null) monto = BigDecimal.ONE;
        String titulo;
        if (su.getMembresia() != null) titulo = "Membresía: " + su.getMembresia().getNombre();
        else if (su.getPlanSuscripcion() != null) titulo = "Plan: " + su.getPlanSuscripcion().getNombre();
        else titulo = "Suscripción " + suscripcionId;

        Map<String, Object> body = new HashMap<>();
        Map<String, Object> item = new HashMap<>();
        item.put("title", titulo);
        item.put("quantity", 1);
        item.put("currency_id", "PEN");
        item.put("unit_price", monto);
        body.put("items", List.of(item));
        String backSuccess = (mpBackSuccess != null && !mpBackSuccess.isBlank()) ? mpBackSuccess : "http://localhost:4200/";
        String backFailure = (mpBackFailure != null && !mpBackFailure.isBlank()) ? mpBackFailure : "http://localhost:4200/";
        String backPending = (mpBackPending != null && !mpBackPending.isBlank()) ? mpBackPending : "http://localhost:4200/";
        Map<String, Object> backUrls = new HashMap<>();
        backUrls.put("success", backSuccess);
        backUrls.put("failure", backFailure);
        backUrls.put("pending", backPending);
        body.put("back_urls", backUrls);
        body.put("auto_return", "approved");
        body.put("binary_mode", true);
        body.put("notification_url", mpNotificationUrl);
        body.put("external_reference", "SUSCRIPCION:" + suscripcionId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mpAccessToken);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        RestTemplate rest = new RestTemplate();
        Map response;
        try {
            response = rest.postForObject("https://api.mercadopago.com/checkout/preferences", entity, Map.class);
        } catch (HttpClientErrorException e) {
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", e.getStatusCode().value());
            errorDetails.put("errorMP", e.getResponseBodyAsString());
            errorDetails.put("message", "Error al crear preferencia en Mercado Pago. Revisar errorMP para detalles.");
            errorDetails.put("back_urls_sent", backUrls);
            return ResponseEntity.status(e.getStatusCode()).body(errorDetails);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "Error al crear preferencia en Mercado Pago");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("suscripcionId", suscripcionId);
        if (response != null) {
            res.put("preferenceId", response.get("id"));
            res.put("init_point", response.get("init_point"));
            res.put("sandbox_init_point", response.get("sandbox_init_point"));
        }
        return ResponseEntity.ok(res);
    }

    private int obtenerDescuento(Long usuarioId) {
        List<SuscripcionUsuario> list = suscripcionUsuarioRepository.findByUsuario_IdAndEstado(usuarioId, SuscripcionUsuario.Estado.ACTIVA);
        LocalDateTime ahora = LocalDateTime.now();
        int max = 0;
        for (SuscripcionUsuario su : list) {
            if (su.getFechaInicio() != null && su.getFechaFin() != null && (su.getFechaInicio().isAfter(ahora) || su.getFechaFin().isBefore(ahora))) continue;
            if (su.getPlanSuscripcion() != null && su.getPlanSuscripcion().getTipo() == com.example.backendgym.domain.PlanSuscripcion.Tipo.DESCUENTO) {
                Integer p = su.getPlanSuscripcion().getDescuentoPorcentaje();
                if (p != null) max = Math.max(max, p);
            }
            if (su.getMembresia() != null && su.getMembresia().getDescuentoPorcentaje() != null) {
                max = Math.max(max, su.getMembresia().getDescuentoPorcentaje());
            }
        }
        return max;
    }

    @Transactional
    public Map<String, Object> eliminarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (pedido.getEstado() != Pedido.EstadoPedido.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden eliminar pedidos CANCELADOS");
        }
        Pago pago = pagoRepository.findAll().stream()
                .filter(x -> x.getPedido().getId().equals(pedido.getId()))
                .findFirst().orElse(null);
        if (pago != null) pagoRepository.delete(pago);
        pedidoRepository.delete(pedido);
        Map<String, Object> res = new HashMap<>();
        res.put("deleted", true);
        res.put("id", id);
        return res;
    }

    public List<Map<String, Object>> listarPagos(String estado) {
        return pagoRepository.findAll().stream()
                .filter(pg -> estado == null || pg.getEstado().name().equalsIgnoreCase(estado))
                .limit(200)
                .map(pg -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", pg.getId());
                    m.put("estado", pg.getEstado());
                    m.put("monto", pg.getMonto());
                    m.put("fechaPago", pg.getFechaPago());
                    m.put("metodoPago", pg.getMetodoPago());
                    m.put("referenciaPago", pg.getReferenciaPago());
                    Pedido p = pg.getPedido();
                    if (p != null) {
                        Map<String, Object> ped = new HashMap<>();
                        ped.put("id", p.getId());
                        ped.put("usuarioId", p.getUsuario() != null ? p.getUsuario().getId() : null);
                        ped.put("estado", p.getEstado());
                        ped.put("total", p.getTotal());
                        ped.put("fechaPedido", p.getFechaPedido());
                        m.put("pedido", ped);
                    }
                    return m;
                })
                .toList();
    }

    public List<Map<String, Object>> listarSuscripciones(String estado, String tipo) {
        return suscripcionUsuarioRepository.findAll().stream()
                .filter(su -> estado == null || su.getEstado().name().equalsIgnoreCase(estado))
                .filter(su -> {
                    if (tipo == null) return true;
                    boolean esM = su.getMembresia() != null;
                    boolean esP = su.getPlanSuscripcion() != null;
                    return ("MEMBRESIA".equalsIgnoreCase(tipo) && esM) || ("PLAN".equalsIgnoreCase(tipo) && esP);
                })
                .limit(200)
                .map(su -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", su.getId());
                    m.put("usuarioId", su.getUsuario() != null ? su.getUsuario().getId() : null);
                    m.put("estado", su.getEstado());
                    m.put("monto", su.getMonto());
                    m.put("fechaInicio", su.getFechaInicio());
                    m.put("fechaFin", su.getFechaFin());
                    if (su.getMembresia() != null) {
                        m.put("tipo", "MEMBRESIA");
                        m.put("nombre", su.getMembresia().getNombre());
                    } else if (su.getPlanSuscripcion() != null) {
                        m.put("tipo", "PLAN");
                        m.put("nombre", su.getPlanSuscripcion().getNombre());
                    } else {
                        m.put("tipo", "OTRO");
                    }
                    return m;
                })
                .toList();
    }

    public List<Map<String, Object>> listarPedidosAdmin(String estado, String estadoPago) {
        List<Pedido> pedidos = pedidoRepository.findAll();
        pedidos.sort((a,b) -> {
            LocalDateTime da = a.getFechaPedido();
            LocalDateTime db = b.getFechaPedido();
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return db.compareTo(da);
        });
        return pedidos.stream()
                .filter(p -> estado == null || p.getEstado().name().equalsIgnoreCase(estado))
                .filter(p -> {
                    if (estadoPago == null) return true;
                    Pago pg = pagoRepository.findAll().stream().filter(x -> x.getPedido().getId().equals(p.getId())).findFirst().orElse(null);
                    return pg != null && pg.getEstado().name().equalsIgnoreCase(estadoPago);
                })
                .filter(p -> { try { return p.getDetalles() != null && !p.getDetalles().isEmpty(); } catch (Exception e) { return false; } })
                .limit(100)
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("usuarioId", p.getUsuario() != null ? p.getUsuario().getId() : null);
                    if (p.getUsuario() != null) {
                        try { m.put("usuarioEmail", p.getUsuario().getEmail()); } catch (Exception ignored) {}
                        try { m.put("usuarioNombre", p.getUsuario().getNombre()); } catch (Exception ignored) {}
                    }
                    m.put("estado", p.getEstado());
                    m.put("metodoPago", p.getMetodoPago());
                    m.put("total", p.getTotal());
                    m.put("fechaPedido", p.getFechaPedido());
                    m.put("fechaConfirmacion", p.getFechaConfirmacion());
                    try {
                        List<Map<String, Object>> det = p.getDetalles().stream().map(d -> {
                            Map<String, Object> dm = new HashMap<>();
                            try { dm.put("productoNombre", d.getProducto() != null ? d.getProducto().getNombre() : null); } catch (Exception ignored) {}
                            dm.put("cantidad", d.getCantidad());
                            dm.put("subtotal", d.getSubtotal());
                            return dm;
                        }).toList();
                        m.put("detalles", det);
                    } catch (Exception ignored) {}
                    Pago pg = pagoRepository.findAll().stream().filter(x -> x.getPedido().getId().equals(p.getId())).findFirst().orElse(null);
                    if (pg != null) {
                        Map<String, Object> pm = new HashMap<>();
                        pm.put("id", pg.getId());
                        pm.put("estado", pg.getEstado());
                        pm.put("monto", pg.getMonto());
                        pm.put("referenciaPago", pg.getReferenciaPago());
                        pm.put("fechaPago", pg.getFechaPago());
                        m.put("pago", pm);
                    }
                    return m;
                })
                .toList();
    }

    public ResponseEntity<Map<String, Object>> crearPreferenciaDirecta(String titulo, BigDecimal monto, Long uid) {
        if (mpAccessToken == null || mpAccessToken.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "MP_TOKEN_MISSING");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
        Usuario usuario = new Usuario();
        usuario.setId(uid);

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setMetodoPago(Pedido.MetodoPago.MERCADO_PAGO);
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setTotal(monto);
        pedido = pedidoRepository.save(pedido);

        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodoPago(Pedido.MetodoPago.MERCADO_PAGO);
        pago.setMonto(pedido.getTotal());
        pago.setEstado(Pago.EstadoPago.PENDIENTE);
        pago.setFechaPago(LocalDateTime.now());
        pago = pagoRepository.save(pago);

        Map<String, Object> body = new HashMap<>();
        Map<String, Object> item = new HashMap<>();
        item.put("title", "Compra AngelsGym - " + titulo + " (#" + pedido.getId() + ")");
        item.put("quantity", 1);
        item.put("currency_id", "PEN");
        item.put("unit_price", pedido.getTotal());
        body.put("items", List.of(item));
        String backSuccess = (mpBackSuccess != null && !mpBackSuccess.isBlank()) ? mpBackSuccess : "http://localhost:4200/";
        String backFailure = (mpBackFailure != null && !mpBackFailure.isBlank()) ? mpBackFailure : "http://localhost:4200/";
        String backPending = (mpBackPending != null && !mpBackPending.isBlank()) ? mpBackPending : "http://localhost:4200/";
        Map<String, Object> backUrls = new HashMap<>();
        backUrls.put("success", backSuccess);
        backUrls.put("failure", backFailure);
        backUrls.put("pending", backPending);
        body.put("back_urls", backUrls);
        body.put("auto_return", "approved");
        body.put("notification_url", mpNotificationUrl);
        body.put("external_reference", String.valueOf(pedido.getId()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mpAccessToken);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        RestTemplate rest = new RestTemplate();
        Map response;
        try {
            response = rest.postForObject("https://api.mercadopago.com/checkout/preferences", entity, Map.class);
        } catch (HttpClientErrorException e) {
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", e.getStatusCode().value());
            errorDetails.put("errorMP", e.getResponseBodyAsString());
            errorDetails.put("message", "Error al crear preferencia en Mercado Pago. Revisar errorMP para detalles.");
            errorDetails.put("back_urls_sent", backUrls);
            return ResponseEntity.status(e.getStatusCode()).body(errorDetails);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "Error al crear preferencia en Mercado Pago");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("pedidoId", pedido.getId());
        res.put("pagoId", pago.getId());
        if (response != null) {
            res.put("preferenceId", response.get("id"));
            res.put("init_point", response.get("init_point"));
            res.put("sandbox_init_point", response.get("sandbox_init_point"));
        }
        return ResponseEntity.ok(res);
    }

    @Transactional
    public Map<String, Object> webhook(Map<String, Object> payload, Map<String, String> params) {
        Map<String, Object> res = new HashMap<>();

        String topic = params != null ? params.getOrDefault("type", params.get("topic")) : null;
        String paymentId = null;
        String merchantOrderId = null;
        String status = null;
        Object extRef = null;

        // 1) Datos básicos desde params
        if (params != null) {
            paymentId = params.get("data.id");
            if (paymentId == null) {
                paymentId = params.get("id");
            }
            merchantOrderId = params.get("merchant_order_id");
        }

        // 2) Datos básicos desde payload
        if (payload != null) {
            if (paymentId == null) {
                Object data = payload.get("data");
                if (data instanceof Map<?, ?> d) {
                    Object did = d.get("id");
                    if (did != null) {
                        paymentId = String.valueOf(did);
                    }
                }
                if (merchantOrderId == null) {
                    Object mo = payload.get("id");
                    if (mo != null) {
                        merchantOrderId = String.valueOf(mo);
                    }
                }
            }

            Object paymentsObj = payload.get("payments");
            if (paymentsObj instanceof List<?> pl && !pl.isEmpty()) {
                Object first = pl.get(0);
                if (first instanceof Map<?, ?> pm) {
                    Object st = pm.get("status");
                    if (st != null) {
                        status = String.valueOf(st);
                    }
                    Object pid = pm.get("id");
                    if (pid != null && paymentId == null) {
                        paymentId = String.valueOf(pid);
                    }
                }
            }

            Object er = payload.get("external_reference");
            if (er != null) {
                extRef = er;
            }
        }

        // 3) Completar desde /v1/payments si falta info
        if ((status == null || extRef == null) && paymentId != null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(mpAccessToken);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                RestTemplate rest = new RestTemplate();
                ResponseEntity<Map> r = rest.exchange(
                        "https://api.mercadopago.com/v1/payments/" + paymentId,
                        HttpMethod.GET,
                        entity,
                        Map.class
                );
                Map body = r.getBody();
                if (body != null) {
                    if (status == null && body.get("status") != null) {
                        status = String.valueOf(body.get("status"));
                    }
                    if (extRef == null && body.get("external_reference") != null) {
                        extRef = body.get("external_reference");
                    }
                    if (merchantOrderId == null && body.get("order") instanceof Map<?, ?> om) {
                        Object moid = om.get("id");
                        if (moid != null) {
                            merchantOrderId = String.valueOf(moid);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 4) Completar desde /merchant_orders si aún falta info
        if ((status == null || extRef == null) && merchantOrderId != null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(mpAccessToken);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                RestTemplate rest = new RestTemplate();
                ResponseEntity<Map> r = rest.exchange(
                        "https://api.mercadopago.com/merchant_orders/" + merchantOrderId,
                        HttpMethod.GET,
                        entity,
                        Map.class
                );
                Map mo = r.getBody();
                if (mo != null) {
                    if (extRef == null && mo.get("external_reference") != null) {
                        extRef = mo.get("external_reference");
                    }
                    if (status == null && mo.get("payments") instanceof List<?> pl && !pl.isEmpty()) {
                        Object first = pl.get(0);
                        if (first instanceof Map<?, ?> pm) {
                            Object st = pm.get("status");
                            if (st != null) {
                                status = String.valueOf(st);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 5) Si el pago está aprobado, actualizar suscripción o pedido
        if (extRef != null && status != null && "approved".equalsIgnoreCase(status)) {
            synchronized (this) {
                String ext = String.valueOf(extRef);
                if (ext.startsWith("SUSCRIPCION:")) {
                    Long sid = Long.valueOf(ext.substring("SUSCRIPCION:".length()));
                    SuscripcionUsuario su = suscripcionUsuarioRepository.findById(sid).orElse(null);
                    if (su != null) {
                        su.setEstado(SuscripcionUsuario.Estado.ACTIVA);
                        su.setFechaAprobacion(LocalDateTime.now());
                        suscripcionUsuarioRepository.save(su);
                        res.put("updated", true);
                    }
                } else {
                    Long pedidoId = Long.valueOf(ext);
                    Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
                    if (pedido != null) {
                        Pago pago = pagoRepository.findAll().stream()
                                .filter(pg -> pg.getPedido().getId().equals(pedidoId))
                                .findFirst().orElse(null);
                        if (pago != null) {
                            if (Pago.EstadoPago.CONFIRMADO.equals(pago.getEstado()) ||
                                    Pedido.EstadoPedido.CONFIRMADO.equals(pedido.getEstado())) {
                                // Idempotente: ya confirmado
                                res.put("alreadyUpdated", true);
                            } else {
                                // Confirmar pago y pedido
                                pago.setEstado(Pago.EstadoPago.CONFIRMADO);
                                if (paymentId != null) {
                                    pago.setReferenciaPago(paymentId);
                                }
                                pago.setFechaPago(LocalDateTime.now());
                                pagoRepository.save(pago);

                                pedido.setEstado(Pedido.EstadoPedido.CONFIRMADO);
                                pedido.setFechaConfirmacion(LocalDateTime.now());
                                pedidoRepository.save(pedido);

                                // Descontar stock según los detalles del pedido
                                if (pedido.getDetalles() != null) {
                                    for (DetallePedido d : pedido.getDetalles()) {
                                        Producto prod = d.getProducto();
                                        if (prod == null) {
                                            continue;
                                        }
                                        try {
                                            Long pid = prod.getId();
                                            if (pid == null) {
                                                continue;
                                            }
                                            Producto dbProd = productoRepository.findById(pid).orElse(null);
                                            if (dbProd == null) {
                                                continue;
                                            }
                                            Integer stock = dbProd.getStock();
                                            int cant = d.getCantidad() != null ? d.getCantidad() : 0;
                                            if (cant <= 0) {
                                                continue;
                                            }
                                            if (stock == null || stock < cant) {
                                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para confirmar el pedido");
                                            }
                                            dbProd.setStock(stock - cant);
                                            productoRepository.save(dbProd);
                                        } catch (ResponseStatusException e) {
                                            throw e;
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }

                                // Limpiar carrito y popularidad
                                Usuario usuario = pedido.getUsuario();
                                List<Carrito> items = carritoRepository.findByUsuarioOrderByFechaAgregadoDesc(usuario);
                                for (Carrito item : items) {
                                    carritoRepository.delete(item);
                                }

                                incrementarPopularidadDePedido(pedido);

                                // Enviar comprobante de pago por correo al usuario (HTML + PDF adjunto)
                                try {
                                    if (usuario != null && usuario.getEmail() != null) {
                                        String correo = usuario.getEmail();
                                        String nombre = usuario.getNombre() != null ? usuario.getNombre() : "Cliente";
                                        BigDecimal total = pedido.getTotal();
                                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                                        String fecha = (pago.getFechaPago() != null ? pago.getFechaPago() : LocalDateTime.now()).format(fmt);

                                        StringBuilder detalle = new StringBuilder();
                                        if (pedido.getDetalles() != null) {
                                            for (DetallePedido d : pedido.getDetalles()) {
                                                String prod = d.getProducto() != null ? d.getProducto().getNombre() : "Producto";
                                                detalle.append("<tr>")
                                                        .append("<td>").append(prod).append("</td>")
                                                        .append("<td style='text-align:center'>").append(d.getCantidad()).append("</td>")
                                                        .append("<td style='text-align:right'>S/ ").append(d.getSubtotal()).append("</td>")
                                                        .append("</tr>");
                                            }
                                        }

                                        String html = String.format(
                                                "<!DOCTYPE html>" +
                                                        "<html lang='es'>" +
                                                        "<head><meta charset='UTF-8'><title>Comprobante de pago</title></head>" +
                                                        "<body style='margin:0;padding:0;background:#0f172a;font-family:Arial,sans-serif;color:#e5e7eb;'>" +
                                                        "<div style='max-width:640px;margin:0 auto;padding:24px;'>" +
                                                        "<div style='background:#020617;border-radius:12px;padding:24px;border:1px solid #1d4ed8;box-shadow:0 0 30px rgba(59,130,246,0.5);'>" +
                                                        "<div style='display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;'>" +
                                                        "<div>" +
                                                        "<div style='font-size:14px;color:#9ca3af;'>Comprobante de pago</div>" +
                                                        "<div style='font-size:20px;font-weight:700;color:#ffffff;'>AngelsGym</div>" +
                                                        "</div>" +
                                                        "<img src='https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRooqX_tEjlq63fL2GQIpdJ50tuKZ7qT-qJ8A&s' alt='Mercado Pago' style='height:40px;' />" +
                                                        "</div>" +
                                                        "<p style='font-size:14px;color:#e5e7eb;'>Hola <strong>%s</strong>, gracias por tu compra.</p>" +
                                                        "<p style='font-size:13px;color:#9ca3af;margin-bottom:16px;'>Fecha: <strong>%s</strong><br/>" +
                                                        "Pedido: <strong>#%d</strong><br/>" +
                                                        "Método de pago: <strong>Mercado Pago</strong></p>" +
                                                        "<table style='width:100%%;border-collapse:collapse;font-size:13px;margin-bottom:12px;'>" +
                                                        "<thead>" +
                                                        "<tr style='background:#0b1120;color:#93c5fd;border-bottom:1px solid #1d4ed8;'>" +
                                                        "<th style='text-align:left;padding:8px;'>Producto</th>" +
                                                        "<th style='text-align:center;padding:8px;width:70px;'>Cant.</th>" +
                                                        "<th style='text-align:right;padding:8px;width:110px;'>Subtotal</th>" +
                                                        "</tr>" +
                                                        "</thead>" +
                                                        "<tbody>%s</tbody>" +
                                                        "<tfoot>" +
                                                        "<tr style='border-top:1px solid #1d4ed8;'>" +
                                                        "<td colspan='2' style='text-align:right;padding:8px;font-weight:600;'>Total</td>" +
                                                        "<td style='text-align:right;padding:8px;font-weight:700;color:#4ade80;'>S/ %s</td>" +
                                                        "</tr>" +
                                                        "</tfoot>" +
                                                        "</table>" +
                                                        "<p style='font-size:12px;color:#9ca3af;margin-top:8px;'>Si tienes alguna duda o problema con tu pago, puedes escribirnos desde nuestra página de consultas.</p>" +
                                                        "</div>" +
                                                        "</div>" +
                                                        "</body></html>",
                                                nombre, fecha, pedido.getId(), detalle.toString(), total
                                        );

                                    // Generar PDF simple con OpenPDF
                                    byte[] pdfBytes = null;
                                    try {
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        Document doc = new Document();
                                        PdfWriter.getInstance(doc, baos);
                                        doc.open();

                                        // Logo de Mercado Pago en el PDF (si se puede descargar)
                                        try {
                                            Image logo = Image.getInstance("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRooqX_tEjlq63fL2GQIpdJ50tuKZ7qT-qJ8A&s");
                                            logo.scaleToFit(80, 80);
                                            logo.setAlignment(Image.ALIGN_RIGHT);
                                            doc.add(logo);
                                        } catch (Exception ignored) {
                                        }

                                        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                                        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
                                        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

                                        doc.add(new Paragraph("Comprobante de pago", titleFont));
                                        doc.add(new Paragraph("\n"));
                                        doc.add(new Paragraph("Fecha: " + fecha, normalFont));
                                        doc.add(new Paragraph("Pedido: #" + pedido.getId(), normalFont));
                                        doc.add(new Paragraph("Cliente: " + nombre, normalFont));
                                        doc.add(new Paragraph("Correo: " + correo, normalFont));
                                        doc.add(new Paragraph("Metodo de pago: Mercado Pago", normalFont));
                                        doc.add(new Paragraph("\n"));

                                        PdfPTable table = new PdfPTable(3);
                                        table.setWidthPercentage(100);
                                        table.setWidths(new float[]{5f, 1.5f, 2.5f});

                                        PdfPCell c1 = new PdfPCell(new Paragraph("Producto", boldFont));
                                        PdfPCell c2 = new PdfPCell(new Paragraph("Cant.", boldFont));
                                        PdfPCell c3 = new PdfPCell(new Paragraph("Subtotal", boldFont));
                                        Color headerBg = new Color(239, 246, 255);
                                        Color headerText = new Color(37, 99, 235);
                                        c1.setBackgroundColor(headerBg);
                                        c2.setBackgroundColor(headerBg);
                                        c3.setBackgroundColor(headerBg);
                                        c1.getPhrase().getFont().setColor(headerText);
                                        c2.getPhrase().getFont().setColor(headerText);
                                        c3.getPhrase().getFont().setColor(headerText);
                                        table.addCell(c1);
                                        table.addCell(c2);
                                        table.addCell(c3);

                                        if (pedido.getDetalles() != null) {
                                            for (DetallePedido d : pedido.getDetalles()) {
                                                String prod = d.getProducto() != null ? d.getProducto().getNombre() : "Producto";
                                                table.addCell(new Paragraph(prod, normalFont));
                                                table.addCell(new Paragraph(String.valueOf(d.getCantidad()), normalFont));
                                                table.addCell(new Paragraph("S/ " + d.getSubtotal(), normalFont));
                                            }
                                        }

                                        PdfPCell totalLabel = new PdfPCell(new Paragraph("Total", boldFont));
                                        totalLabel.setColspan(2);
                                        table.addCell(totalLabel);
                                        table.addCell(new Paragraph("S/ " + total, boldFont));

                                        doc.add(table);
                                        doc.add(new Paragraph("\n"));
                                        Font small = FontFactory.getFont(FontFactory.HELVETICA, 9);
                                        doc.add(new Paragraph("Si tienes alguna duda o problema con tu pago, puedes escribirnos en:", small));
                                        Font linkFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.BLUE);
                                        doc.add(new Paragraph("http://localhost:4200/consultas", linkFont));
                                        doc.close();
                                        pdfBytes = baos.toByteArray();
                                    } catch (DocumentException de) {
                                        System.err.println("[PDF] Error generando comprobante: " + de.getMessage());
                                    }

                                        mailService.sendHtmlWithAttachment(
                                                correo,
                                                "Comprobante de pago AngelsGym",
                                                html,
                                                pdfBytes,
                                                "comprobante-pedido-" + pedido.getId() + ".pdf",
                                                "application/pdf");
                                    }
                                } catch (Exception ignored) {
                                }

                                res.put("updated", true);
                            }
                        }
                    }
                }
            }
        }

        res.put("ok", true);
        res.put("topic", topic);
        res.put("paymentId", paymentId);
        res.put("merchantOrderId", merchantOrderId);
        return res;
    }
}
