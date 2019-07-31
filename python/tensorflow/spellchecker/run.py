from rnn_lm import RNNLM
import os

# due to https://github.com/tensorflow/tensorflow/issues/12414
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'

import tensorflow as tf

# Set TRAIN to true will build a new model
TRAIN = True

# If VERBOSE is true, then print the ppl of every sequence when we
# are testing.
VERBOSE = True

# this is the path where all data files live
# TODO join paths os independently
data_path= '/home/jose/spark-nlp-models/data/medical/'

# To indicate your test/train corpora
model_name = 'bigone'
train_file = model_name + ".txt.ids"
classes_file = model_name + ".txt.classes"
vocab_file = model_name + ".txt.vocab"
valid_file = "valid.ids"

model_path = './model/best_model.ckpt'

with open(data_path + train_file) as fp:
    num_train_samples = len(fp.readlines())
with open(data_path + valid_file) as fp:
    num_valid_samples = len(fp.readlines())

vocab_path = data_path + vocab_file
with open(vocab_path) as vocab:
    vocab_size = len(vocab.readlines())

def test_sentences():
    '''
    these are crazy sentences to test the algorithm
    :return:
    '''
    sentences = ['she came to me in an unexpected gesture',
                 'she came to me in an unexpected day',
                 'she came to me in an unexpected car',
                 'she came to me in an unexpected morning',
                 'she came to me in an unexpected way',
                 'she came to me in an unexpected dream']

    candidates = ['gesture', 'day', 'car', 'morning', 'way', 'dream']

    return (sentences, candidates)

def create_model(sess):
    #with tf.device('/job:localhost/replica:0/task:0/device:XLA_GPU:0'):
    _model = RNNLM(vocab_size=vocab_size,
                  batch_size=24,
                  num_epochs=3,
                  check_point_step= 20000,
                  num_train_samples=num_train_samples,
                  num_valid_samples=num_valid_samples,
                  num_layers=1,
                  num_hidden_units=300,
                  initial_learning_rate=.7,
                  final_learning_rate=0.0005,
                  max_gradient_norm=5.0,
                  )
    sess.run(tf.global_variables_initializer())
    return _model

if TRAIN:
    gpu_options = tf.GPUOptions(per_process_gpu_memory_fraction=0.99)
    with tf.Session(config=tf.ConfigProto(log_device_placement=True, allow_soft_placement=True)) as sess: # gpu_options=gpu_options,
        model = create_model(sess)
        model.load_classes(data_path + classes_file)
        model.load_vocab(vocab_path)
        train_ids = data_path + train_file
        valid_ids = data_path + valid_file
        saver = tf.train.Saver()
        model.batch_train(sess, saver, train_ids, valid_ids)

tf.reset_default_graph()
with tf.Session(config=tf.ConfigProto(allow_soft_placement=True)) as sess:
    from time import time

    model = create_model(sess)
    model.load_classes(data_path + classes_file)
    model.load_vocab(vocab_path)
    saver = tf.train.Saver()
    saver.restore(sess, model_path)
    model.save('bundle', sess)
    sents, cands = test_sentences()

    t0 = time()
    for _ in range(10000):
        model.predict_(sess, cands)
    print(time() - t0)

    t0 = time()
    for _ in range(10000):
        model.predict(sess, sents)
    print(time() - t0)

