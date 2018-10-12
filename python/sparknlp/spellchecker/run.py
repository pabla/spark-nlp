from data_prepare import gen_vocab
from data_prepare import gen_id_seqs
from rnn_lm import RNNLM
import os
# It seems that there are some little bugs in tensorflow 1.4.1.
# You can find more details in
# https://github.com/tensorflow/tensorflow/issues/12414
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'

import tensorflow as tf

# Set TRAIN to true will build a new model
TRAIN = True 

# If VERBOSE is true, then print the ppl of every sequence when we
# are testing.
VERBOSE = True

# To indicate your test corpus
test_file = "./gap_filling_exercise/gap_filling_exercise"

#if not os.path.isfile("../../../../auxdata/spell_dataset/vocab/spell_corpus.txt.ids"):
#    gen_vocab("ptb/train")
#if not os.path.isfile("data/train.ids"):
#    gen_id_seqs("ptb/train")
#    gen_id_seqs("ptb/valid")

with open("../../../../auxdata/spell_dataset/vocab/spell_corpus.txt.ids") as fp:
    num_train_samples = len(fp.readlines())
with open("../../../../auxdata/spell_dataset/vocab/valid.ids") as fp:
    num_valid_samples = len(fp.readlines())

with open("../../../../auxdata/spell_dataset/vocab/vocab") as vocab:
    vocab_size = len(vocab.readlines())

def create_model(sess):
    model = RNNLM(vocab_size=vocab_size,
                  batch_size=1,
                  num_epochs=6,
                  check_point_step=100,
                  num_train_samples=num_train_samples,
                  num_valid_samples=num_valid_samples,
                  num_layers=2,
                  num_hidden_units=100,
                  initial_learning_rate=.6,
                  final_learning_rate=0.0005,
                  max_gradient_norm=5.0,
                  )
    sess.run(tf.global_variables_initializer())
    return model

if TRAIN:
    gpu_options = tf.GPUOptions(per_process_gpu_memory_fraction=0.99)
    with tf.Session(config=tf.ConfigProto(gpu_options=gpu_options, log_device_placement=True, device_count = {'GPU': 0})) as sess:
        model = create_model(sess)
        saver = tf.train.Saver()
        model.batch_train(sess, saver)

tf.reset_default_graph()
gpu_options = tf.GPUOptions(per_process_gpu_memory_fraction=0.99)


with tf.Session(config=tf.ConfigProto(gpu_options=gpu_options)) as sess:
    model = create_model(sess)
    saver = tf.train.Saver()
    saver.restore(sess, "model/best_model.ckpt")

    model.save('bundle', sess)

    predict_id_file = os.path.join("data", test_file.split("/")[-1]+".ids")
    if not os.path.isfile(predict_id_file):
        gen_id_seqs(test_file)
    model.predict(sess, predict_id_file, test_file, verbose=VERBOSE)

