import os
import shelve
from flask import request, jsonify, Response, stream_with_context
from llms.llm import LLM

# Ensure the data directory exists
DATA_DIR = "./data"
if not os.path.exists(DATA_DIR):
    os.makedirs(DATA_DIR)

# Name of the persistent store file.
SESSION_DB = os.path.join(DATA_DIR, "sessions.db")
DEFAULT_CONTEXT = """
You are a minecraft villager. You are focused on living your own life. Your responses are always as short as possible.
"""


def register_handlers(app):

    @app.route("/chat", methods=["POST"])
    def chat():
        # Get and validate input
        data = request.json
        input_text = data.get("input")
        player = data.get("player")
        villager = data.get("villager")
        if not input_text:
            return jsonify({"error": "Input text is required"}), 400
        if not player or not villager:
            return jsonify({"error": "Both player and villager are required"}), 400

        # Define a session key based on the villager-player pair.
        session_key = f"{villager}-{player}"

        # Open the persistent shelve store.
        with shelve.open(SESSION_DB, writeback=True) as db:
            # Retrieve the stored LLM instance if it exists.
            if session_key in db:
                llm = db[session_key]
            else:
                # Initialize a new LLM instance with the default context.
                llm = LLM(DEFAULT_CONTEXT)
                db[session_key] = llm

            # Stream the response using the stored LLM instance.
            response_stream = llm.chat(input_text, stream=True)

            def stream_and_store():
                for chunk in response_stream:
                    yield chunk
                # After streaming, save the updated LLM instance back to the store.
                db[session_key] = llm

            return Response(stream_with_context(stream_and_store()), content_type="text/plain")
