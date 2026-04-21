import { useEffect, useRef, useState, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import type { IMessage } from '@stomp/stompjs';

export interface Notificacion {
    tipo: string;
    mensaje: string;
    citaId: number;
    pacienteNombre: string;
    medicoLogin?: string;
    rutaAccion?: string;
    archivoDescarga?: string;
    accionLabel?: string;
    timestamp: string;
}

/**
 * Hook personalizado para conectar al WebSocket STOMP del backend.
 * Usa WebSocket nativo (sin SockJS) para compatibilidad con Vite/ESM.
 *
 * @param topicos El o los tópicos STOMP a los que suscribirse
 * @param enabled Si está habilitado o no
 */
export function useWebSocket(topicos: string[], enabled = true) {
    const clientRef = useRef<Client | null>(null);
    const [notificaciones, setNotificaciones] = useState<Notificacion[]>([]);
    const [connected, setConnected] = useState(false);

    const clearNotificacion = useCallback((index: number) => {
        setNotificaciones(prev => prev.filter((_, i) => i !== index));
    }, []);

    const clearAll = useCallback(() => {
        setNotificaciones([]);
    }, []);

    useEffect(() => {
        if (!enabled) return;

        // Construir URL de WebSocket nativo basado en el protocolo actual
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/ws`;

        const client = new Client({
            brokerURL: wsUrl,
            reconnectDelay: 5000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            debug: (str) => {
                if (import.meta.env.DEV) {
                    console.debug('[WS]', str);
                }
            },
            onConnect: () => {
                setConnected(true);
                topicos.forEach((topico) => {
                    client.subscribe(topico, (message: IMessage) => {
                        try {
                            const body: Notificacion = JSON.parse(message.body);
                            setNotificaciones(prev => [body, ...prev]);
                        } catch (e) {
                            console.error('[WS] Error parsing message:', e);
                        }
                    });
                });
            },
            onDisconnect: () => {
                setConnected(false);
                console.log('[WS] Desconectado');
            },
            onStompError: (frame) => {
                console.error('[WS] STOMP Error:', frame.headers['message']);
            },
        });

        clientRef.current = client;
        client.activate();

        return () => {
            if (clientRef.current?.connected) {
                clientRef.current.deactivate();
            }
        };
    }, [enabled, topicos]);

    return { notificaciones, connected, clearNotificacion, clearAll };
}
