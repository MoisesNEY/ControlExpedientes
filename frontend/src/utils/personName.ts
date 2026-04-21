export function normalizeDisplayText(value?: string | null): string | null {
    if (!value) {
        return null;
    }

    const normalized = value.trim();
    if (!normalized || normalized.toLowerCase() === 'null' || normalized.toLowerCase() === 'undefined') {
        return null;
    }

    return normalized;
}

export function buildFullName(parts: Array<string | null | undefined>, fallback: string): string {
    const fullName = parts
        .map(normalizeDisplayText)
        .filter((value): value is string => Boolean(value))
        .join(' ')
        .trim();

    return fullName || fallback;
}
