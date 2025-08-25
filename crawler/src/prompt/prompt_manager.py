def generate_classification_prompt(text):

    with open("data/prompt/classification_prompt.txt", "r", encoding="utf-8") as f:
        PROMPT_TEMPLATE = f.read()

    prompt = PROMPT_TEMPLATE.format(text=text)

    return prompt


def generate_extraction_prompt(text):
    with open("data/prompt/extraction_prompt.txt", "r", encoding="utf-8") as f:
        PROMPT_TEMPLATE = f.read()

    prompt = PROMPT_TEMPLATE.format(text=text)

    return prompt
