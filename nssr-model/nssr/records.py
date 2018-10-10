import tensorflow as tf
import numpy as np
import json


def build_dataset(record, map):
    return tf.data.TextLineDataset([record]).map(map)


def json_map(json_str, feature_label):
    json_data = json.loads(json_str)
    peaks = decomp_bitset(json_data["peaks"]).flatten()
    peaks.resize((1 << 12,), refcheck=False)
    features = json_data[feature_label]
    if feature_label == "fingerprint":
        features = decomp_bitset(features).flatten()
        features.resize((1 << 16,), refcheck=False)
    return features, peaks


def smile_map(tensor):
    result = tf.py_func(lambda js: json_map(js, "smile"),
                        [tensor],
                        (tf.string, tf.uint8))
    return result


def fingerprint_map(tensor):
    result = tf.py_func(lambda js: json_map(js, "fingerprint"),
                        [tensor],
                        (tf.uint8, tf.uint8))
    return result


def decomp_bitset(bitset):
    result = np.ndarray((len(bitset), 64), dtype=np.uint8)
    for n in range(len(bitset)):
        result[n] = decomp_word(bitset[n])
    return result


def decomp_word(word):
    result = np.empty(64, dtype=np.uint8)
    for n in range(64):
        result[n] = bool((word & (1 << n)) != 0)
    return result


if __name__ == '__main__':
    dataset = build_dataset("records/filtered/all_records_filtered_1H.json", fingerprint_map)
    sess = tf.Session()
    iterator = dataset.make_one_shot_iterator()
    next_t = iterator.get_next()
    for x in range(10):
        print(sess.run(next_t))
