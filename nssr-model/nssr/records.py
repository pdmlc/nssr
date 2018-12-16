import tensorflow as tf
import numpy as np
import json
import datetime


def build_dataset(record: str, map) -> tf.data.TextLineDataset:
    return tf.data.TextLineDataset([record]).map(map)


def json_map(json_str: str, feature_label: str):
    json_data: dict = json.loads(json_str)
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


def decomp_bitset(bitset: list) -> np.ndarray:
    result: np.ndarray = np.ndarray((len(bitset), 64), dtype=np.float32)
    for n in range(len(bitset)):
        result[n] = decomp_word(bitset[n])
    return result


def decomp_word(word: int) -> np.ndarray:
    result: np.ndarray = np.empty(64, dtype=np.float32)
    for n in range(64):
        result[n] = bool((word & (1 << n)) != 0)
    return result


if __name__ == '__main__':
    print(datetime.datetime.today().strftime('%Y-%m-%d_T%H.%M.%S.%f'))

    dataset: tf.data.TextLineDataset = build_dataset("records/filtered/all_records_filtered_1H.json", fingerprint_map)

    dataset.shuffle(1 << 12)

    sz: int = 17027
    tf.logging.set_verbosity(tf.logging.DEBUG)

    train: tf.data.Dataset = dataset.take(int(0.9 * sz)).batch(32)
    test: tf.data.Dataset = dataset.take(int(0.1 * sz)).batch(32)

    print("dataset", dataset)
    print("train", train.output_shapes)
    print("test", test.output_shapes)

    model: tf.keras.models.Sequential = tf.keras.models.Sequential([
        tf.keras.layers.Dense(1 << 13,
                              activation='relu',
                              kernel_initializer='zeros',
                              bias_initializer='zeros',
                              input_shape=(1, 1 << 14),
                              batch_size=32
                              ),
        tf.keras.layers.Dense(1 << 12,
                              activation='relu',
                              kernel_initializer='zeros',
                              bias_initializer='zeros',
                              ),
    ])

    model.compile(optimizer=tf.keras.optimizers.Adam(lr=0.01),
                  loss=tf.keras.losses.cosine_proximity,
                  metrics=[tf.keras.losses.cosine_proximity, 'acc'])
    model.summary()
    model.fit(train, epochs=12, steps_per_epoch=432)
    model.save('nssr_fp201_{}.hd5'.format(datetime.datetime.today().strftime('%Y-%m-%d_T%H.%M.%S.%f')))

    # model = tf.keras.models.load_model('nssr_fp201_2018-10-17_T23.58.39.125514.hd5')

    score = model.evaluate(test, steps=50, verbose=1)
    print(score)
