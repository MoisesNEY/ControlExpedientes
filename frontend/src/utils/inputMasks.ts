export const formatNicaraguanCedula = (value: string) => {
  const cleaned = value.toUpperCase().replace(/[^0-9A-Z]/g, '');
  const digits = cleaned.replace(/[^0-9]/g, '').slice(0, 13);
  const suffix = digits.length === 13 ? cleaned.replace(/[^A-Z]/g, '').slice(0, 1) : '';

  let formatted = digits.slice(0, 3);

  if (digits.length > 3) {
    formatted += `-${digits.slice(3, 9)}`;
  }

  if (digits.length > 9) {
    formatted += `-${digits.slice(9, 13)}`;
  }

  return `${formatted}${suffix}`;
};

export const isValidNicaraguanCedula = (value?: string) => {
  if (!value) return true;
  return /^\d{3}-\d{6}-\d{4}[A-Z]$/.test(value.toUpperCase());
};

export const formatPhoneNumber = (value: string) => {
  let digits = value.replace(/\D/g, '');

  if (digits.length > 8 && digits.startsWith('505')) {
    digits = digits.slice(3);
  }

  digits = digits.slice(0, 8);

  if (digits.length <= 4) {
    return digits;
  }

  return `${digits.slice(0, 4)}-${digits.slice(4, 8)}`;
};

export const isValidPhoneNumber = (value?: string) => {
  if (!value) return true;
  return /^\d{4}-\d{4}$/.test(value);
};