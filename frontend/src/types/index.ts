export type Role = 'ROLE_PATIENT' | 'ROLE_DOCTOR' | 'ROLE_ADMIN';

export interface AuthUser {
  userId: number;
  login: string;
  fullName: string;
  avatarUrl?: string | null;
  roles: Role[];
}

export interface JwtResponse extends AuthUser {
  accessToken: string;
  tokenType: string;
}

export interface UserResponse {
  id: number;
  login: string;
  email: string;
  fullName: string;
  phone?: string | null;
  avatarUrl?: string | null;
  enabled: boolean;
  roles: Role[];
  createdAt: string;
}

export interface DoctorResponse {
  id: number;
  userId: number;
  login: string;
  fullName: string;
  email: string;
  phone?: string | null;
  avatarUrl?: string | null;
  specialization: string;
  bio?: string | null;
  photoUrl?: string | null;
  available: boolean;
  workStart: string;
  workEnd: string;
  roomNumber?: string | null;
}

export interface PatientResponse {
  id: number;
  userId: number;
  login: string;
  fullName: string;
  email: string;
  phone?: string | null;
  avatarUrl?: string | null;
  birthDate?: string | null;
  address?: string | null;
  insuranceNumber?: string | null;
  enabled: boolean;
}

export type AppointmentStatus = 'PLANNED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';

export interface AppointmentResponse {
  id: number;
  patientId: number;
  patientFullName: string;
  doctorId: number;
  doctorFullName: string;
  doctorSpecialization: string;
  appointmentDate: string;
  status: AppointmentStatus;
  notes?: string | null;
  calendarEventId?: string | null;
  rescheduleCount: number;
  createdAt: string;
}

export interface SymptomResponse {
  id: number;
  name: string;
  description?: string | null;
}

export interface DiagnosisResponse {
  id: number;
  name: string;
  description?: string | null;
  specialization: string;
  symptomIds: number[];
}

export interface RecommendationResponse {
  id: number;
  patientId: number;
  symptomIds: number[];
  diagnosisId?: number | null;
  diagnosisName?: string | null;
  recommendedDoctorId?: number | null;
  recommendedDoctorName?: string | null;
  recommendedDoctorSpecialization?: string | null;
  confidence: number;
  createdAt: string;
}

export interface MedicalRecordResponse {
  id: number;
  patientId: number;
  patientFullName: string;
  doctorId: number;
  doctorFullName: string;
  appointmentId?: number | null;
  diagnosis: string;
  treatment?: string | null;
  notes?: string | null;
  createdAt: string;
}

export interface OverviewStats {
  totalUsers: number;
  totalPatients: number;
  totalDoctors: number;
  totalAppointments: number;
  plannedAppointments: number;
  completedAppointments: number;
  cancelledAppointments: number;
  totalSymptoms: number;
  totalDiagnoses: number;
  appointmentsBySpecialization: { specialization: string; count: number }[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
