import { useState, useEffect } from 'react'
import axios from 'axios'
import './index.css'

function App() {
    const [rates, setRates] = useState({ usdToPln: 0, plnToUsd: 0 })
    const [amount, setAmount] = useState(1)
    const [sourceCurrency, setSourceCurrency] = useState('USD')
    const [targetCurrency, setTargetCurrency] = useState('PLN')
    const [convertedAmount, setConvertedAmount] = useState(null)

    const [history, setHistory] = useState([])
    const [sortOrder, setSortOrder] = useState('default')

    useEffect(() => {
        fetchRates();
        fetchHistory();
    }, [])

    useEffect(() => {
        fetchHistory();
    }, [sortOrder])

    const fetchRates = async () => {
        try {
            const response = await axios.get('/api/rates');
            setRates({
                usdToPln: response.data.usdToPln,
                plnToUsd: response.data.plnToUsd
            });
        } catch (error) {
            console.error("Failed to fetch rates", error);
        }
    }

    const fetchHistory = async () => {
        try {
            const url = sortOrder === 'default' ? '/api/history' : `/api/history?sort=${sortOrder}`;
            const response = await axios.get(url);
            setHistory(response.data);
        } catch (error) {
            console.error("Failed to fetch history", error);
        }
    }

    const handleConvert = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post('/api/convert', {
                sourceCurrency,
                targetCurrency,
                amount: parseFloat(amount)
            });
            setConvertedAmount(response.data.result);
            fetchHistory(); // Refresh history
        } catch (error) {
            console.error("Conversion failed", error);
        }
    }

    const swapCurrencies = () => {
        setSourceCurrency(targetCurrency);
        setTargetCurrency(sourceCurrency);
        setConvertedAmount(null);
    }

    const handleAmountChange = (e) => {
        setAmount(e.target.value);
        setConvertedAmount(null);
    }

    return (
        <>
            <div className="card">
                <h1>Konwerter USD na PLN</h1>
                <p>Aktualny Kurs: 1 USD = {rates.usdToPln?.toFixed(4)} PLN</p>

                <form onSubmit={handleConvert} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', alignItems: 'center' }}>
                    <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                        <input
                            type="number"
                            value={amount}
                            onChange={handleAmountChange}
                            step="0.01"
                            min="0"
                        />
                        <span>{sourceCurrency}</span>
                        <button type="button" onClick={swapCurrencies}>⇄</button>
                        <span>{targetCurrency}</span>
                    </div>

                    <button type="submit">Przelicz</button>
                </form>

                <h2 style={{ marginTop: '20px', visibility: convertedAmount !== null ? 'visible' : 'hidden' }}>
                    {amount} {sourceCurrency} = {convertedAmount !== null ? convertedAmount.toFixed(2) : '0.00'} {targetCurrency}
                </h2>
            </div>

            <div className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                    <h2>Historia</h2>
                    <select value={sortOrder} onChange={(e) => setSortOrder(e.target.value)}>
                        <option value="default">Najnowsze</option>
                        <option value="highest">Najwyższa Wartość</option>
                        <option value="lowest">Najniższa Wartość</option>
                    </select>
                </div>

                <ul style={{ listStyle: 'none', padding: 0, textAlign: 'left' }}>
                    {history.map((item) => (
                        <li key={item.id} style={{ padding: '10px', borderBottom: '1px solid #444', display: 'flex', justifyContent: 'space-between' }}>
                            <span>{new Date(item.timestamp).toLocaleTimeString()}</span>
                            <span>{item.amount} {item.sourceCurrency} -> <b>{item.result.toFixed(2)} {item.targetCurrency}</b></span>
                        </li>
                    ))}
                </ul>
            </div>
        </>
    )
}

export default App
