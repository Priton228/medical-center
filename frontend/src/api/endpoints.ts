import { api } from './client';
import type {
  AppointmentResponse, AppointmentStatus, DiagnosisResponse, DoctorResponse,
  JwtResponse, MedicalRecordResponse, OverviewStats, Page,
  PatientResponse, RecommendationResponse, SymptomResponse, UserResponse,
} from '@/types';

export const authApi = {
  login: (username: string, password: string) =>
    api.post<JwtResponse>('/api/v1/auth/login', { username, password }).then((r) => r.data),
  register: (payload: any) =>
    api.post<JwtResponse>('/api/v1/auth/register', payload).then((r) => r.data),
  me: () => api.get<UserResponse>('/api/v1/auth/me').then((r) => r.data),
};

export const usersApi = {
  list: (page = 0, size = 50) =>
    api.get<Page<UserResponse>>(`/api/v1/users?page=${page}&size=${size}`).then((r) => r.data),
  setEnabled: (id: number, enabled: boolean) =>
    api.patch<UserResponse>(`/api/v1/users/${id}/enabled`, { enabled }).then((r) => r.data),
  updateMe: (data: { email: string; fullName: string; phone?: string }) =>
    api.put<UserResponse>('/api/v1/users/me', data).then((r) => r.data),
};

export const adminUsersApi = {
  create: (data: {
    username: string; email: string; password: string;
    fullName: string; phone?: string | null; roles: string[];
  }) => api.post<UserResponse>('/api/v1/admin/users', data).then((r) => r.data),
  update: (id: number, data: {
    email: string; fullName: string; phone?: string | null;
    enabled?: boolean; roles?: string[]; newPassword?: string | null;
  }) => api.put<UserResponse>(`/api/v1/admin/users/${id}`, data).then((r) => r.data),
  setRoles: (id: number, roles: string[]) =>
    api.patch<UserResponse>(`/api/v1/admin/users/${id}/roles`, { roles }).then((r) => r.data),
  delete: (id: number) =>
    api.delete(`/api/v1/admin/users/${id}`).then((r) => r.data),
};

export const doctorsApi = {
  list: (page = 0, size = 100, specialization?: string) => {
    const params = new URLSearchParams();
    params.set('page', String(page));
    params.set('size', String(size));
    if (specialization) params.set('specialization', specialization);
    return api.get<Page<DoctorResponse>>(`/api/v1/doctors?${params}`).then((r) => r.data);
  },
  get: (id: number) => api.get<DoctorResponse>(`/api/v1/doctors/${id}`).then((r) => r.data),
  me: () => api.get<DoctorResponse>('/api/v1/doctors/me').then((r) => r.data),
  updateSchedule: (data: { workStart: string; workEnd: string; available: boolean }) =>
    api.put<DoctorResponse>('/api/v1/doctors/me/schedule', data).then((r) => r.data),
  create: (data: any) => api.post<DoctorResponse>('/api/v1/doctors', data).then((r) => r.data),
  update: (id: number, data: any) => api.put<DoctorResponse>(`/api/v1/doctors/${id}`, data).then((r) => r.data),
  delete: (id: number) => api.delete(`/api/v1/doctors/${id}`).then((r) => r.data),
};

export const patientsApi = {
  list: (page = 0, size = 100) =>
    api.get<Page<PatientResponse>>(`/api/v1/patients?page=${page}&size=${size}`).then((r) => r.data),
  me: () => api.get<PatientResponse>('/api/v1/patients/me').then((r) => r.data),
  updateMe: (data: { birthDate?: string | null; address?: string | null; insuranceNumber?: string | null }) =>
    api.put<PatientResponse>('/api/v1/patients/me', data).then((r) => r.data),
  create: (data: any) => api.post<PatientResponse>('/api/v1/patients', data).then((r) => r.data),
  update: (id: number, data: any) => api.put<PatientResponse>(`/api/v1/patients/${id}`, data).then((r) => r.data),
  get: (id: number) => api.get<PatientResponse>(`/api/v1/patients/${id}`).then((r) => r.data),
};

export const appointmentsApi = {
  create: (data: { doctorId: number; appointmentDate: string; notes?: string }) =>
    api.post<AppointmentResponse>('/api/v1/appointments', data).then((r) => r.data),
  listAll: (page = 0, size = 50) =>
    api.get<Page<AppointmentResponse>>(`/api/v1/appointments?page=${page}&size=${size}`).then((r) => r.data),
  listMine: (page = 0, size = 50) =>
    api.get<Page<AppointmentResponse>>(`/api/v1/appointments/me?page=${page}&size=${size}`).then((r) => r.data),
  upcoming: () => api.get<AppointmentResponse[]>('/api/v1/appointments/me/upcoming').then((r) => r.data),
  setStatus: (id: number, status: AppointmentStatus) =>
    api.patch<AppointmentResponse>(`/api/v1/appointments/${id}/status`, { status }).then((r) => r.data),
};

export const symptomsApi = {
  list: () => api.get<SymptomResponse[]>('/api/v1/symptoms').then((r) => r.data),
  search: (q: string) => api.get<SymptomResponse[]>(`/api/v1/symptoms?q=${encodeURIComponent(q)}`).then((r) => r.data),
  create: (data: { name: string; description?: string }) =>
    api.post<SymptomResponse>('/api/v1/symptoms', data).then((r) => r.data),
  update: (id: number, data: { name: string; description?: string }) =>
    api.put<SymptomResponse>(`/api/v1/symptoms/${id}`, data).then((r) => r.data),
  delete: (id: number) => api.delete(`/api/v1/symptoms/${id}`).then((r) => r.data),
};

export const diagnosesApi = {
  list: () => api.get<DiagnosisResponse[]>('/api/v1/diagnoses').then((r) => r.data),
  create: (data: { name: string; description?: string; specialization: string; symptomIds?: number[] }) =>
    api.post<DiagnosisResponse>('/api/v1/diagnoses', data).then((r) => r.data),
  update: (id: number, data: { name: string; description?: string; specialization: string; symptomIds?: number[] }) =>
    api.put<DiagnosisResponse>(`/api/v1/diagnoses/${id}`, data).then((r) => r.data),
  delete: (id: number) => api.delete(`/api/v1/diagnoses/${id}`).then((r) => r.data),
};

export const recommendationsApi = {
  analyze: (symptomIds: number[]) =>
    api.post<RecommendationResponse>('/api/v1/recommendations/analyze', { symptomIds }).then((r) => r.data),
  history: () => api.get<RecommendationResponse[]>('/api/v1/recommendations/me').then((r) => r.data),
};

export const recordsApi = {
  mine: () => api.get<MedicalRecordResponse[]>('/api/v1/medical-records/me').then((r) => r.data),
  byDoctor: () => api.get<MedicalRecordResponse[]>('/api/v1/medical-records/by-doctor').then((r) => r.data),
  ofPatient: (id: number) => api.get<MedicalRecordResponse[]>(`/api/v1/medical-records/patient/${id}`).then((r) => r.data),
  create: (data: { patientId?: number; appointmentId?: number; diagnosis: string; treatment?: string; notes?: string }) =>
    api.post<MedicalRecordResponse>('/api/v1/medical-records', data).then((r) => r.data),
};

export const adminApi = {
  overview: () => api.get<OverviewStats>('/api/v1/admin/stats/overview').then((r) => r.data),
};
