import os
from dotenv import load_dotenv
from openai import OpenAI

# 1. Force reload .env
load_dotenv(override=True)

api_key = os.getenv("LLM_API_KEY")
base_url = os.getenv("LLM_BASE_URL")
model = os.getenv("LLM_MODEL")

print("-" * 30)
print("Configuration Check:")
print(f"LLM_BASE_URL: '{base_url}'")
print(f"LLM_MODEL:    '{model}'")

if not api_key:
    print("LLM_API_KEY:  [MISSING] ❌")
else:
    # Show first/last few chars to verify no hidden spaces
    masked = f"{api_key[:4]}...{api_key[-4:]}"
    print(f"LLM_API_KEY:  '{masked}' (Length: {len(api_key)})")
    if " " in api_key:
        print("⚠️ WARNING: API Key contains spaces! Please remove them in .env")

print("-" * 30)

# 2. Test Connection
try:
    print("\nTesting connection to DeepSeek...")
    client = OpenAI(api_key=api_key, base_url=base_url)
    response = client.chat.completions.create(
        model=model,
        messages=[{"role": "user", "content": "Hi"}],
    )
    print("✅ Connection Success!")
    print("Response:", response.choices[0].message.content)
except Exception as e:
    print("❌ Connection Failed:")
    print(e)
