package com.example.backendgym.service;

import com.example.backendgym.domain.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaypalService {

    @Value("${paypal.client-id:}")
    private String clientId;

    @Value("${paypal.client-secret:}")
    private String clientSecret;

    @Value("${paypal.api-base:https://api-m.sandbox.paypal.com}")
    private String apiBase;

    @Value("${app.front.base-url:http://localhost:4200/}")
    private String frontBaseUrl;

    private final PagoService pagoService;

    public PaypalService(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    private String normalizeFrontBaseUrl() {
        String base = frontBaseUrl == null || frontBaseUrl.isBlank() ? "http://localhost:4200/" : frontBaseUrl;
        if (!base.endsWith("/")) base = base + "/";
        return base;
    }

    private String getAccessToken() {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PAYPAL_CREDENTIALS_MISSING");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        RestTemplate rest = new RestTemplate();
        Map response;
        try {
            response = rest.postForObject(apiBase + "/v1/oauth2/token", entity, Map.class);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(e.getStatusCode(), "Error al obtener token de PayPal");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al comunicarse con PayPal");
        }
        Object token = response != null ? response.get("access_token") : null;
        if (token == null) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PAYPAL_TOKEN_EMPTY");
        return token.toString();
    }

    @Transactional
    public Map<String, Object> crearOrdenDesdeCarrito(Long usuarioId) {
        Map<String, Object> base = pagoService.iniciarPedidoDesdeCarrito(usuarioId, Pedido.MetodoPago.PAYPAL);
        Object pid = base.get("pedidoId");
        Object pgid = base.get("pagoId");
        Object m = base.get("monto");
        if (pid == null || pgid == null || m == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PAYPAL_ORDER_DATA_MISSING");
        }
        Long pedidoId = ((Number) pid).longValue();
        Long pagoId = ((Number) pgid).longValue();
        BigDecimal monto = (BigDecimal) m;

        String accessToken = getAccessToken();

        Map<String, Object> amount = new HashMap<>();
        amount.put("currency_code", "USD");
        amount.put("value", monto.toPlainString());

        Map<String, Object> pu = new HashMap<>();
        pu.put("reference_id", String.valueOf(pedidoId));
        pu.put("amount", amount);

        Map<String, Object> body = new HashMap<>();
        body.put("intent", "CAPTURE");
        body.put("purchase_units", List.of(pu));

        String baseUrl = normalizeFrontBaseUrl();
        Map<String, Object> appContext = new HashMap<>();
        appContext.put("return_url", baseUrl + "pagos?paypalOrderId={orderId}&pedidoId=" + pedidoId);
        appContext.put("cancel_url", baseUrl + "carrito?paypalCancel=true");
        body.put("application_context", appContext);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        RestTemplate rest = new RestTemplate();
        Map response;
        try {
            response = rest.postForObject(apiBase + "/v2/checkout/orders", entity, Map.class);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(e.getStatusCode(), "PAYPAL_CREATE_ERROR");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear orden en PayPal");
        }

        String approvalUrl = null;
        String orderId = null;
        if (response != null) {
            Object id = response.get("id");
            if (id != null) orderId = id.toString();
            Object links = response.get("links");
            if (links instanceof List) {
                for (Object o : (List) links) {
                    if (o instanceof Map) {
                        Map link = (Map) o;
                        Object rel = link.get("rel");
                        Object href = link.get("href");
                        if (rel != null && "approve".equalsIgnoreCase(rel.toString()) && href != null) {
                            approvalUrl = href.toString();
                            break;
                        }
                    }
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("pedidoId", pedidoId);
        res.put("pagoId", pagoId);
        res.put("approvalUrl", approvalUrl);
        res.put("paypalOrderId", orderId);
        return res;
    }

    @Transactional
    public Map<String, Object> capturarOrden(String orderId, Long pedidoId) {
        String accessToken = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        RestTemplate rest = new RestTemplate();
        Map response;
        try {
            response = rest.postForObject(apiBase + "/v2/checkout/orders/" + orderId + "/capture", entity, Map.class);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(e.getStatusCode(), "PAYPAL_CAPTURE_ERROR");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al capturar orden en PayPal");
        }
        String status = null;
        if (response != null && response.get("status") != null) {
            status = response.get("status").toString();
        }
        Map<String, Object> res = new HashMap<>();
        res.put("paypalStatus", status);
        if (status != null && status.equalsIgnoreCase("COMPLETED")) {
            Map<String, Object> base = pagoService.confirmarPedidoUsuario(pedidoId, orderId);
            res.putAll(base);
        }
        return res;
    }
}
