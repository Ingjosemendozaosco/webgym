import { HttpParams } from '@angular/common/http';
export function toHttpParams(obj: any): HttpParams {
  let params = new HttpParams();
  Object.entries(obj || {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') params = params.set(k, String(v));
  });
  return params;
}
