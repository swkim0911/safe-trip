def generate_classification_prompt(text):

    with open("data/prompt/classification_prompt.txt", "r", encoding="utf-8") as f:
        PROMPT_TEMPLATE = f.read()

    prompt = PROMPT_TEMPLATE.format(text=text)

    return prompt

def get_classification_system_content():
    
    with open("data/prompt/classification_system_content.txt", "r", encoding="utf-8") as f:
        system_content = f.read()
        
    return system_content

def generate_parsing_prompt(text):
    with open("data/prompt/parsing_prompt.txt", "r", encoding="utf-8") as f:
        PROMPT_TEMPLATE = f.read()

    prompt = PROMPT_TEMPLATE.format(text=text)

    return prompt

def get_parsing_system_content():
    
    with open("data/prompt/parsing_system_content.txt", "r", encoding="utf-8") as f:
        system_content = f.read()
        
    return system_content
