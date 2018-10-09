import tensorflow as tf
import json


def build_dataset(record, map):
    return tf.data.TextLineDataset([record]).map(map)


def json_map(json_str, feature):
    json_data = json.loads(json_str)
    return json_data[feature], json_data["peaks"]


def smile_map(tensor):
    result = tf.py_func(lambda js: json_map(js, "smile"),
                        [tensor],
                        (tf.string, tf.float64))
    return result


def fingerprint_map(tensor):
    result = tf.py_func(lambda js: json_map(js, "fingerprint"),
                        [tensor],
                        (tf.int8, tf.float64))
    return result


if __name__ == '__main__':
    dataset = build_dataset("records/filtered/all_records_filtered_1H.json", smile_map)
    sess = tf.Session()
    iterator = dataset.make_one_shot_iterator()
    next_t = iterator.get_next()
    for x in range(10):
        print(sess.run(next_t))
