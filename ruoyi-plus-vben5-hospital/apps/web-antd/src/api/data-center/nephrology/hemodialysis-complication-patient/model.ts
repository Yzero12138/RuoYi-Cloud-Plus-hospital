export interface HemodialysisComplicationPatient {
  id?: number;
  medicalRecordNo?: string;
  patientName?: string;
  gender?: number;
  birthDate?: string;
  age?: number;
  idCardNo?: string;
  admissionDepartment?: string;
  dischargeDepartment?: string;
  complicationDiagnosis?: string;
  complicationCode?: string;
  admissionCondition?: number;
  admissionTime?: string;
  dischargeTime?: string;
  isDeleted?: number;
  createTime?: string;
  updateTime?: string;
}
