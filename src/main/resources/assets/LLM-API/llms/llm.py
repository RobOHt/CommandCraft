import os
from openai import OpenAI
from collections import deque

class LLM:
    """
    A Python class for interacting with DeepSeek-V3's API, with built-in memory management.
    """

    def __init__(self, context: str, base_url: str = "https://api.deepseek.com"):
        """
        Initialize the DeepSeek API client and set up conversation memory.
        """
        self.base_url = base_url
        self.refresh_api_key()
        self.messages = deque(maxlen=100)  # Store up to 100 messages in memory
        self.add_message("system", context)

    def refresh_api_key(self):
        """
        Refresh the API key from the environment variable before each chat session.
        """
        api_key = os.getenv("DEEPSEEK_API_KEY")
        if not api_key:
            raise ValueError(
                "The environment variable `DEEPSEEK_API_KEY` is not set. "
                "To set it permanently:\n"
                "Linux/macOS: Add `export DEEPSEEK_API_KEY='your_api_key'` to your `~/.bashrc` or `~/.zshrc` file.\n"
                "Windows: Use `setx DEEPSEEK_API_KEY 'your_api_key'` in Command Prompt.\n"
                "Then restart your terminal or IDE."
            )
        self.client = OpenAI(api_key=api_key, base_url=self.base_url)

    def add_message(self, role: str, content: str):
        """
        Add a message to the conversation history.
        """
        self.messages.append({"role": role, "content": content})

    def chat(self, new_message: str, model: str = "deepseek-chat", stream: bool = False):
        """
        Send a chat request to the DeepSeek-V3 API, automatically managing message history.
        """
        self.refresh_api_key()  # Ensure the latest API key is used
        self.add_message("user", new_message)  # Add the new user message to history

        try:
            response = self.client.chat.completions.create(
                model=model,
                messages=list(self.messages),  # Send the conversation history
                stream=stream
            )

            if stream:
                # Stream response and save it in memory
                def stream_response():
                    assistant_reply = ""
                    for chunk in response:
                        if chunk.choices[0].delta.content:
                            content = chunk.choices[0].delta.content
                            assistant_reply += content
                            yield content
                    self.add_message("assistant", assistant_reply)  # Save assistant's full reply

                return stream_response()

            else:
                # Return the full response and save it in memory
                assistant_reply = response.choices[0].message.content
                self.add_message("assistant", assistant_reply)
                return assistant_reply

        except Exception as e:
            print(f"API request failed: {str(e)}")
            return None

    def __getstate__(self):
        """
        Prepare the object state for pickling. Remove the live API client since it is not serializable.
        """
        state = self.__dict__.copy()
        if "client" in state:
            del state["client"]
        return state

    def __setstate__(self, state):
        """
        Restore the object state from the unpickled state and reinitialize the API client.
        """
        self.__dict__.update(state)
        self.refresh_api_key()


# Example usage for testing only.
if __name__ == "__main__":
    # Example chat request
    context = "You are a minecraft villager. You are focused on living your own life. Your answers are super concise."
    message = "How are you?"

    # Initialize the LLM client
    llm = LLM(context)

    # Streaming response
    while True:
        message = input("Player: ")
        print("Villager:", end=" ", flush=True)
        for chunk in llm.chat(message, stream=True):
            print(chunk, end="", flush=True)
        print()
