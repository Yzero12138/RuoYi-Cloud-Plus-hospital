export interface PdPatient {
  id?: number;
  inpatientNo?: string;
  outpatientNo?: string;
  name?: string;
  gender?: string;
  age?: number;
  birthDate?: string;
  idcardMask?: string;
  idcardHash?: string;
  admitDate?: string;
  dischargeDate?: string;
  admitDept?: string;
  dischargeDept?: string;
  diagName?: string;
  diagCode?: string;
  sourcePatientId?: string;
  sourceUpdatedAt?: string;
  createdAt?: string;
  updatedAt?: string;
  idCard?: string;
  minAdmitDate?: string;
}
