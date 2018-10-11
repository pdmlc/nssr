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
        features.resize((1 << 14,), refcheck=False)
    return features, peaks


def smile_map(tensor):
    result = tf.py_func(lambda js: json_map(js, "smile"),
                        [tensor],
                        (tf.string, tf.uint8))
    return result


def fingerprint_map(tensor):
    result = tf.py_func(lambda js: json_map(js, "fingerprint"),
                        [tensor],
                        (tf.float32, tf.float32))
    x, y = result
    x.set_shape((1, 1 << 14))
    y.set_shape((1, 1 << 12))
    result = x, y
    return result


def decomp_bitset(bitset):
    result = np.ndarray((len(bitset), 64), dtype=np.float32)
    for n in range(len(bitset)):
        result[n] = decomp_word(bitset[n])
    return result


def decomp_word(word):
    result = np.empty(64, dtype=np.float32)
    for n in range(64):
        result[n] = bool((word & (1 << n)) != 0)
    return result


if __name__ == '__main__':

    dataset = build_dataset("records/filtered/all_records_filtered_1H.json", fingerprint_map)

    dataset.shuffle(1 << 12)

    sz = 17027
    tf.logging.set_verbosity(tf.logging.DEBUG)

    train = dataset.take(int(0.9 * sz))
    test = dataset.take(int(0.1 * sz))

    print("dataset", dataset)
    print("train", train.output_shapes)
    print("test", test.output_shapes)

    model = tf.keras.models.Sequential([
        tf.keras.layers.Dense(1 << 13, activation=tf.nn.relu, input_dim=(1 << 14)),
        tf.keras.layers.Dense(1 << 12, activation=tf.nn.relu),
    ])

    model.compile(optimizer=tf.train.AdamOptimizer(0.01),
                  loss='mse',
                  metrics=['mae'])
    model.summary()
    model.fit(train, epochs=10, steps_per_epoch=30)
    model.eval(test, steps=30)


