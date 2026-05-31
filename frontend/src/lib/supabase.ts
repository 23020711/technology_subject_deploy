import { createClient } from '@supabase/supabase-js'

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL;
const supabaseKey = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY;

if (!supabaseUrl || !supabaseKey) {
    throw new Error('Missing Supabase environment variables')
}
export const supabase = createClient(supabaseUrl, supabaseKey);

function resolveBackendUrl(): string {
    const raw = import.meta.env.VITE_API_BASE_URL as string | undefined;
    const trimmed = raw != null ? String(raw).trim().replace(/\/$/, '') : '';
    if (trimmed.length > 0) {
        return trimmed;
    }
    return 'http://localhost:8080';
}

export const BACKEND_URL = resolveBackendUrl();

export interface UserProfile {
    id: string
    email: string
    name: string
    plan: string
    phone: string | null
    theme: string
    language: string
    created_at: string
    updated_at: string | null
    premium_expires_at: string | null
}
