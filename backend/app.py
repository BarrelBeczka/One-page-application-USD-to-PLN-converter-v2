import os
from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from datetime import datetime

# ==========================================
# KONFIGURACJA BAZY DANYCH
# Zmień hasło poniżej jeśli uruchamiasz to na uczelni:
# np. HASLO_DO_BAZY = "student"
# ==========================================
HASLO_DO_BAZY = "5636d5ca"
UZYTKOWNIK = "postgres"
NAZWA_BAZY = "currency_db"
HOST = "localhost"
PORT = "5432"

app = Flask(__name__)
CORS(app) # Obsługa CORS dla Reacta (localhost:5173)

app.config['SQLALCHEMY_DATABASE_URI'] = f'postgresql://{UZYTKOWNIK}:{HASLO_DO_BAZY}@{HOST}:{PORT}/{NAZWA_BAZY}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)

# ==========================================
# MODELE DANYCH
# ==========================================
class CurrencyRate(db.Model):
    __tablename__ = 'currency_rate'
    currency_pair = db.Column(db.String, primary_key=True)
    rate = db.Column(db.Float, nullable=False)

class ConversionHistory(db.Model):
    __tablename__ = 'conversion_history'
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    source_currency = db.Column(db.String, nullable=False)
    target_currency = db.Column(db.String, nullable=False)
    amount = db.Column(db.Float, nullable=False)
    result = db.Column(db.Float, nullable=False)
    rate = db.Column(db.Float, nullable=False)
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)

    def to_dict(self):
        return {
            'id': self.id,
            'sourceCurrency': self.source_currency,
            'targetCurrency': self.target_currency,
            'amount': self.amount,
            'result': self.result,
            'rate': self.rate,
            # React oczekuje konkretnego formatowania czasu dla .getTime()
            'timestamp': self.timestamp.isoformat() + 'Z' if self.timestamp else None
        }

# ==========================================
# ENDPOINTY API
# ==========================================
@app.route('/api/rates', methods=['GET'])
def get_rates():
    rate_record = CurrencyRate.query.filter_by(currency_pair='USD/PLN').first()
    usd_to_pln = rate_record.rate if rate_record else 3.95
    pln_to_usd = 1.0 / usd_to_pln if usd_to_pln else 0.25
    return jsonify({
        'usdToPln': usd_to_pln,
        'plnToUsd': pln_to_usd
    })

@app.route('/api/convert', methods=['POST'])
def convert():
    data = request.json
    source_curr = data.get('sourceCurrency', '').upper()
    target_curr = data.get('targetCurrency', '').upper()
    amount = float(data.get('amount', 0))

    rate_record = CurrencyRate.query.filter_by(currency_pair='USD/PLN').first()
    usd_to_pln = rate_record.rate if rate_record else 3.95
    pln_to_usd = 1.0 / usd_to_pln if usd_to_pln else 0.25

    if source_curr == 'USD' and target_curr == 'PLN':
        rate = usd_to_pln
        result = amount * rate
    elif source_curr == 'PLN' and target_curr == 'USD':
        rate = pln_to_usd
        result = amount * rate
    else:
        return jsonify({'error': 'Unsupported currency pair'}), 400

    history = ConversionHistory(
        source_currency=source_curr,
        target_currency=target_curr,
        amount=amount,
        result=result,
        rate=rate
    )
    db.session.add(history)
    db.session.commit()

    return jsonify(history.to_dict())

@app.route('/api/history', methods=['GET'])
def get_history():
    sort = request.args.get('sort', 'default')
    
    query = ConversionHistory.query.order_by(ConversionHistory.timestamp.desc())
    history_records = query.all()

    if sort == 'highest':
        history_records.sort(key=lambda x: x.result, reverse=True)
    elif sort == 'lowest':
        history_records.sort(key=lambda x: x.result)

    return jsonify([h.to_dict() for h in history_records])

# ==========================================
# INICJALIZACJA BAZY DANYCH
# ==========================================
def setup_database():
    with app.app_context():
        db.create_all()
        # Początkowy kurs (seedowanie bazy, odpowiednik data.sql z Javy)
        if not CurrencyRate.query.filter_by(currency_pair='USD/PLN').first():
            initial_rate = CurrencyRate(currency_pair='USD/PLN', rate=3.95)
            db.session.add(initial_rate)
            db.session.commit()

if __name__ == '__main__':
    setup_database()
    # Port 8080 - tak jak w starej aplikacji Javowej, by nie trzeba było zmieniać proxy we Vite
    app.run(port=8080, debug=True)
