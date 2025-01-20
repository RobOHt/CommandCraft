from flask import Flask
from api.handlers import register_handlers

app = Flask(__name__)

# Register API endpoints
register_handlers(app)

if __name__ == "__main__":
    app.run(port=5000)
