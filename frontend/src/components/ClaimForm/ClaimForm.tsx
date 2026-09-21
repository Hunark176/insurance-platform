import { useState } from 'react';
import './ClaimForm.css';
import { fetchClaims } from '../../api/claimService';
import type { Claim } from '../../types/claim';

export function ClaimForm() {
    const [claims, setClaims] = useState<Claim[]>([]);
    const [message, setMessage] = useState('');

    const handleLoadClaims = async () => {
        try {
            const data = await fetchClaims();

            setClaims(data);
            setMessage('Daten erfolgreich geladen!');
        } catch (error) {
            console.error(error);
            setMessage('Keine Verbindung zum Server möglich.');
        }
    };

    return (
        <div className="formContainer">
            <h2>Claims aus Datenbank laden</h2>

            <button
                type="button"
                onClick={handleLoadClaims}
                className="submitButton"
                style={{ marginBottom: '20px' }}
            >
                Daten abrufen (GET)
            </button>

            {message && <p className="message">{message}</p>}

            <div style={{ marginTop: '10px' }}>
                <h3>Vorhandene Claims:</h3>

                <pre
                    style={{
                        background: '#eee',
                        padding: '20px',
                        borderRadius: '14px',
                        maxHeight: '200px',
                        overflow: 'auto',
                    }}
                >
                    {JSON.stringify(claims, null, 2)}
                </pre>
            </div>
        </div>
    );
}

export default ClaimForm;