from googletrans import Translator
translator = Translator()
def trans_en(query):
    result = translator.translate(query, dest='en')
    return str((result.text))
