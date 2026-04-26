import { Navigate, Route, Routes } from 'react-router-dom';
import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';
import RoleGuard from '@/components/RoleGuard';
import PatientLayout from '@/layouts/PatientLayout';
import DoctorLayout from '@/layouts/DoctorLayout';
import AdminLayout from '@/layouts/AdminLayout';
import PatientDashboard from '@/pages/patient/PatientDashboard';
import PatientDoctors from '@/pages/patient/PatientDoctors';
import PatientAppointments from '@/pages/patient/PatientAppointments';
import PatientSymptoms from '@/pages/patient/PatientSymptoms';
import PatientRecords from '@/pages/patient/PatientRecords';
import PatientProfile from '@/pages/patient/PatientProfile';
import DoctorDashboard from '@/pages/doctor/DoctorDashboard';
import DoctorSchedule from '@/pages/doctor/DoctorSchedule';
import DoctorAppointments from '@/pages/doctor/DoctorAppointments';
import DoctorRecords from '@/pages/doctor/DoctorRecords';
import DoctorProfile from '@/pages/doctor/DoctorProfile';
import AdminDashboard from '@/pages/admin/AdminDashboard';
import AdminUsers from '@/pages/admin/AdminUsers';
import AdminDoctors from '@/pages/admin/AdminDoctors';
import AdminPatients from '@/pages/admin/AdminPatients';
import AdminSymptoms from '@/pages/admin/AdminSymptoms';
import AdminDiagnoses from '@/pages/admin/AdminDiagnoses';
import AdminProfile from '@/pages/admin/AdminProfile';
import HomeRedirect from '@/pages/HomeRedirect';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<RoleGuard role="ROLE_PATIENT"><PatientLayout /></RoleGuard>}>
        <Route path="/patient" element={<PatientDashboard />} />
        <Route path="/patient/doctors" element={<PatientDoctors />} />
        <Route path="/patient/appointments" element={<PatientAppointments />} />
        <Route path="/patient/symptoms" element={<PatientSymptoms />} />
        <Route path="/patient/records" element={<PatientRecords />} />
        <Route path="/patient/profile" element={<PatientProfile />} />
      </Route>

      <Route element={<RoleGuard role="ROLE_DOCTOR"><DoctorLayout /></RoleGuard>}>
        <Route path="/doctor" element={<DoctorDashboard />} />
        <Route path="/doctor/schedule" element={<DoctorSchedule />} />
        <Route path="/doctor/appointments" element={<DoctorAppointments />} />
        <Route path="/doctor/records" element={<DoctorRecords />} />
        <Route path="/doctor/profile" element={<DoctorProfile />} />
      </Route>

      <Route element={<RoleGuard role="ROLE_ADMIN"><AdminLayout /></RoleGuard>}>
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/users" element={<AdminUsers />} />
        <Route path="/admin/doctors" element={<AdminDoctors />} />
        <Route path="/admin/patients" element={<AdminPatients />} />
        <Route path="/admin/symptoms" element={<AdminSymptoms />} />
        <Route path="/admin/diagnoses" element={<AdminDiagnoses />} />
        <Route path="/admin/profile" element={<AdminProfile />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
