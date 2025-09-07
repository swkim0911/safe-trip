def generate_classification_prompt(text):

    with open("data/prompt/classification_prompt.txt", "r", encoding="utf-8") as f:
        PROMPT_TEMPLATE = f.read()

    prompt = PROMPT_TEMPLATE.format(text=text)

    return prompt


def generate_parsing_prompt(text):
    with open("data/prompt/parsing_prompt.txt", "r", encoding="utf-8") as f:
        PROMPT_TEMPLATE = f.read()

    prompt = PROMPT_TEMPLATE.format(text=text)

    return prompt
