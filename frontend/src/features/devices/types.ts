export interface Device {
  id: number;
  name: string;
  // Maximum hourly consumption, in kWh
  maxConsumption: number;
  // Owner of the device; null while it is not assigned
  userId: number | null;
}

export interface DeviceRequest {
  name: string;
  maxConsumption: number;
}