import os
import json
from openai import OpenAI
from dotenv import load_dotenv

# Load environment variables
load_dotenv(override=True)

class LLMClient:
    def __init__(self):
        # Support for generic OpenAI-compatible APIs (DeepSeek, Moonshot, etc.)
        self.api_key = os.getenv("LLM_API_KEY")
        self.base_url = os.getenv("LLM_BASE_URL", "https://api.openai.com/v1")
        self.model = os.getenv("LLM_MODEL", "gpt-3.5-turbo")
        
        if not self.api_key:
            print("Warning: LLM_API_KEY is not set.")
            self.client = None
        else:
            self.client = OpenAI(api_key=self.api_key, base_url=self.base_url)

    def chat_completion(self, system_prompt, user_content, temperature=0.7):
        """
        Send a chat completion request to the LLM.
        """
        if not self.client:
            return {"error": "LLM client not initialized. Check API Key."}

        try:
            response = self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_content}
                ],
                temperature=temperature
            )
            return response.choices[0].message.content
        except Exception as e:
            print(f"LLM API Error: {e}")
            return {"error": str(e)}

    def analyze_json(self, system_prompt, data, response_format=None):
        """
        Helper for JSON analysis tasks.
        Data is converted to JSON string and sent.
        """
        user_content = json.dumps(data, ensure_ascii=False, indent=2)
        return self.chat_completion(system_prompt, user_content)
