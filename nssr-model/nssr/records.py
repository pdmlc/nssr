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
                        (tf.float32, tf.float32))
    x, y = result
    x.set_shape((65536, 1))
    y.set_shape((4096, 1))
    result = x, y
    print(result)
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
    sess = tf.Session()

    dataset = build_dataset("records/filtered/all_records_filtered_1H.json", fingerprint_map)

    dataset.shuffle(4096)

    sz = 17027

    train = dataset.take(int(0.9 * sz))
    test = dataset.take(int(0.1 * sz))

    next_train = train.make_one_shot_iterator().get_next()
    next_test = test.make_one_shot_iterator().get_next()
    for x in range(10):
        print("train", train.output_shapes)
        print("test", test.output_shapes)

    model = tf.keras.models.Sequential([
        tf.keras.layers.Flatten(input_shape=(65536, 1)),
        tf.keras.layers.Dense(32768, activation='relu'),
        tf.keras.layers.Dense(16384, activation='relu'),
        tf.keras.layers.Dense(8192, activation='relu'),
        tf.keras.layers.Dense(4096, activation='relu'),
        tf.keras.layers.Activation('relu')
    ])

    model.compile(optimizer=tf.train.AdamOptimizer(0.01),
                  loss='mse',
                  metrics=['mae'])

    model.fit(dataset, epochs=10, steps_per_epoch=30)
    model.eval(dataset, steps=30)


