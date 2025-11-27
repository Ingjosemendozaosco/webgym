export interface AuthRequest { email: string; password: string; }
export interface RegisterDTO { nombre: string; email: string; password: string; }
export interface AuthResponse { token: string; }
