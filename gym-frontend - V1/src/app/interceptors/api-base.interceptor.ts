import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../environments/environment';

export const apiBaseInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith('/api')) {
    return next(req);
  }

  const base = environment.apiBaseUrl.endsWith('/') ? environment.apiBaseUrl.slice(0, -1) : environment.apiBaseUrl;
  const url = `${base}${req.url}`;
  const updated = req.clone({ url });
  return next(updated);
};
