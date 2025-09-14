from utils.prompt_utils import get_classification_system_content, generate_classification_prompt, get_parsing_system_content, generate_parsing_prompt
import tiktoken, os

MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
ENC = tiktoken.encoding_for_model(MODEL)

def estimate_classification_request_tokens(body):
    system_content = get_classification_system_content()
    prompt = generate_classification_prompt(body)

    # 메시지 2개(system + user) 오버헤드 고려
    overhead = 8

    return (
            len(ENC.encode(system_content)) +
            len(ENC.encode(prompt)) +
            overhead
    )

def estimate_parsing_request_tokens(body):
    system_content = get_parsing_system_content()
    prompt = generate_parsing_prompt(body)

    # 메시지 2개(system + user) 오버헤드 고려
    overhead = 8

    return (
            len(ENC.encode(system_content)) +
            len(ENC.encode(prompt)) +
            overhead
    )