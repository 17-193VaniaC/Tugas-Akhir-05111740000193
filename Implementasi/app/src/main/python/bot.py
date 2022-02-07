from firebase import firebase
from absl import logging

import tensorflow.compat.v1 as tf
tf.disable_v2_behavior()

import tensorflow_hub as hub
import sentencepiece as spm
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity

firebase = firebase.FirebaseApplication("https://bandungzoochatbot-default-rtdb.firebaseio.com/", None)
brainfile_data = firebase.get('/Brainfile/', '')
pertanyaan = []
jawaban = []
for data in brainfile_data:
  qna = data
  if(type(qna) is dict):
    pertanyaan.append(qna.get('pertanyaan'))
    jawaban.append(qna.get('jawaban'))

NO_ANSWER = "Mohon maaf pertanyaan anda tidak dapat dipahami, silahkan tanya kembali"
module = hub.Module("https://tfhub.dev/google/universal-sentence-encoder-lite/2")

input_placeholder = tf.sparse_placeholder(tf.int64, shape=[None, None])
encodings = module(
    inputs=dict(     values=input_placeholder.values,
        indices=input_placeholder.indices,
        dense_shape=input_placeholder.dense_shape))

with tf.Session() as sess:
  spm_path = sess.run(module(signature="spm_path"))

sp = spm.SentencePieceProcessor()
with tf.io.gfile.GFile(spm_path, mode="rb") as f:
  sp.LoadFromSerializedProto(f.read())

def getTensorSparseFormat(sp, sentences):
  ids = [sp.EncodeAsIds(x) for x in sentences]
  max_len = max(len(x) for x in ids)
  dense_shape=(len(ids), max_len)
  values=[item for sublist in ids for item in sublist]
  indices=[[row,col] for row in range(len(ids)) for col in range(len(ids[row]))]
  return (values, indices, dense_shape)

def getAnswer(q_user):
  new_questions = pertanyaan.copy()
  new_questions.append(q_user)
  values, indices, dense_shape = getTensorSparseFormat(sp, new_questions)
  with tf.Session() as session:
    session.run([tf.global_variables_initializer(), tf.tables_initializer()])
    message_embeddings = session.run(
        encodings,
        feed_dict={input_placeholder.values: values,
                  input_placeholder.indices: indices,
                  input_placeholder.dense_shape: dense_shape})
    cosine = cosine_similarity(message_embeddings, message_embeddings[:-1])
    del message_embeddings
    del new_questions
    del values, indices, dense_shape
    if(max(cosine[-1])<0.5):
      return NO_ANSWER
    else:
      return jawaban[np.argmax((cosine[-1]))]