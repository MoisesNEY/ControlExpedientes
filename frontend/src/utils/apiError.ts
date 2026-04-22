import axios from 'axios';

const readBlobMessage = async (blob: Blob): Promise<string | null> => {
  const text = (await blob.text()).trim();
  if (!text) {
    return null;
  }

  try {
    const parsed = JSON.parse(text) as Record<string, unknown>;
    return extractErrorMessageFromObject(parsed) ?? text;
  } catch {
    return text;
  }
};

const extractErrorMessageFromObject = (value: unknown): string | null => {
  if (!value || typeof value !== 'object') {
    return null;
  }

  const record = value as Record<string, unknown>;
  for (const key of ['detail', 'message', 'error', 'title']) {
    const candidate = record[key];
    if (typeof candidate === 'string' && candidate.trim()) {
      return candidate;
    }
  }
  return null;
};

export const getApiErrorMessage = async (error: unknown, fallback: string): Promise<string> => {
  if (axios.isAxiosError(error)) {
    const responseData = error.response?.data;

    if (responseData instanceof Blob) {
      return (await readBlobMessage(responseData)) ?? fallback;
    }

    if (typeof responseData === 'string' && responseData.trim()) {
      return responseData;
    }

    const structuredMessage = extractErrorMessageFromObject(responseData);
    if (structuredMessage) {
      return structuredMessage;
    }
  }

  if (error instanceof Error && error.message.trim()) {
    return error.message;
  }

  return fallback;
};
