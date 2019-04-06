import sys
import numpy as np
import pandas as pd
import xml.etree.ElementTree as ET
from scipy import interpolate

def read_data_from_file(file_path):
    costs = []
    times = []
    xml = ET.parse(file_path)
    for ic in xml.getroot().find("intermediateCosts").iter("ic"):
        costs.append(float(ic.get("cost")))
        times.append(int(ic.get("timeInMs")))
    return costs, times

if __name__ == "__main__":
    output_path = sys.argv[1]
    input_paths = sys.argv[2:]

    costs_by_file, times_by_file = zip(*[read_data_from_file(path) for path in input_paths])

    all_times = [time for times in times_by_file for time in times]
    all_times.append(0)
    all_times.append(10*60*1000)
    all_times = list(set(all_times))
    all_times.sort()

    max_time = all_times[-1]

    x_new = np.array(all_times)

    data = {"time in milliseconds": all_times}
    
    for i in range(len(input_paths)):
        times_by_file[i].append(max_time)
        costs_by_file[i].append(min(costs_by_file[i]))

        times_by_file[i].append(0)
        costs_by_file[i].append(max(costs_by_file[i]))

        x = np.array(times_by_file[i])
        y = np.array(costs_by_file[i])
        f = interpolate.interp1d(x, y)
        y_new = f(x_new)

        data[f"cost {i+1}"] = y_new.tolist()

    df = pd.DataFrame(data)
    df.to_csv(path_or_buf=output_path, sep="\t", index=False, decimal=",")