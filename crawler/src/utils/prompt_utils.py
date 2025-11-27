from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent.parent

def generate_classification_prompt(text):
    filename = BASE_DIR / "data" / "prompt" / "classification" / "active" / "prompt.txt" 
    with open(filename, "r", encoding="utf-8") as f:
        PROMPT_TEMPLATE = f.read()

    prompt = PROMPT_TEMPLATE.format(text=text)

    return prompt

def get_classification_system_content():
    filename = BASE_DIR / "data" / "prompt" / "classification" / "active" / "system_content.txt"
    with open(filename, "r", encoding="utf-8") as f:
        system_content = f.read()
        
    return system_content

def generate_parsing_prompt(text):
    filename = BASE_DIR / "data" / "prompt" / "parsing" / "active" / "prompt.txt"
    with open(filename, "r", encoding="utf-8") as f:
        PROMPT_TEMPLATE = f.read()

    prompt = PROMPT_TEMPLATE.format(text=text)

    return prompt

def get_parsing_system_content():
    filename = BASE_DIR / "data" / "prompt" / "parsing" / "active" / "system_content.txt"
    with open(filename, "r", encoding="utf-8") as f:
        system_content = f.read()
        
    return system_content
