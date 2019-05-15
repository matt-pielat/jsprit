import sys
import numpy as np
import pandas as pd

class TableMaker:
    def __init__(self, file, df):
        self.file = file
        self.df = df

    def write_line(self, text = ""):
        self.file.write(text + "\n")

    def write(self, text):
        self.file.write(text)

    def __write_multirow(self, text, rows = 1):
        self.write(r'\multirow')
        self.write("{%d}{*}{%s}" % (rows, text))

    def __write_multicolumn(self, text, columns = 1, alignment = 'l'):
        self.write(r'\multicolumn')
        self.write("{%d}{%s}{%s}" % (columns, alignment, text))

    def __write_multirowmulticolumn(self, text, columns, rows, alignment):
        self.write(r'\multicolumn')
        self.write("{%d}{%s}{" % (columns, alignment))
        self.__write_multirow(text, rows)
        self.write("}")

    def __format_header(self, text):
        return text

    def __format_cell(self, text):
        return text

    def __format_id(self, text, m, a, t):
        identifiers = ''
        if m: identifiers += 'm'
        if a: identifiers += 'a'
        if t: identifiers += 't'
        if identifiers == '':
            return text
        return text + "$^{%s}$" % (identifiers)

    def __format_value(self, id, value):
        if np.isnan(value):
            output = "b.d."
            if id.startswith('E'):
                output += "$^{%s}$" % (r'\dagger')
            if id.startswith('M'):
                output += "$^{%s}$" % (r'*')
            return output
        return "{:.2f}".format(value).replace('.', ',')

    def write_opening_statements(self):
        self.write_line(r'\begin{center}')
        self.write_line(r'\tiny')
        self.write_line(r'\begin{longtable}{|l|r|rrr|rrr|}')

    def write_header(self):
        self.__write_multirowmulticolumn(self.__format_header(r'id'), 1, 2, '|c|')
        self.write(' & ')
        self.__write_multirowmulticolumn(self.__format_header(r'najlepsze znane'), 1, 2, 'c|')
        self.write(' & ')
        self.__write_multicolumn(self.__format_header(r'jSprit'), 3, 'c|')
        self.write(' & ')
        self.__write_multicolumn(self.__format_header(r'EH-DVRP'), 3, 'c|')
        self.write_line(r' \\ \cline{3-8}')

        self.__write_multicolumn('', 1, '|c|')
        self.write(r' & ')
        self.__write_multicolumn('', 1, 'c|')
        self.write(r' & ')
        self.__write_multicolumn(self.__format_header(r'minimum'), 1, 'c')
        self.write(r' & ')
        self.__write_multicolumn(self.__format_header(r'średnia'), 1, 'c')
        self.write(r' & ')
        self.__write_multicolumn(self.__format_header(r'$\sigma$'), 1, 'c|')
        self.write(r' & ')
        self.__write_multicolumn(self.__format_header(r'minimum'), 1, 'c')
        self.write(r' & ')
        self.__write_multicolumn(self.__format_header(r'średnia'), 1, 'c')
        self.write(r' & ')
        self.__write_multicolumn(self.__format_header(r'$\sigma$'), 1, 'c|')
        self.write_line(r' \\')

    def write_rows(self):
        for index, row in self.df.iterrows():
            self.write(self.__format_cell(self.__format_id(row['id'], row['matrix based distance'], row['asymmetric transport'], row['time windows'])))
            self.write(' & ')
            self.write(self.__format_cell(self.__format_value(row['id'], row['best known cost'])))
            self.write(' & ')
            self.write(self.__format_cell(self.__format_value(row['id'], row['jsprit best cost'])))
            self.write(' & ')
            self.write(self.__format_cell(self.__format_value(row['id'], row['jsprit cost mean'])))
            self.write(' & ')
            self.write(self.__format_cell(self.__format_value(row['id'], row['jsprit cost stddev'])))
            self.write(' & ')
            self.write(self.__format_cell(self.__format_value(row['id'], row['eh-dvrp best cost'])))
            self.write(' & ')
            self.write(self.__format_cell(self.__format_value(row['id'], row['eh-dvrp cost mean'])))
            self.write(' & ')
            self.write(self.__format_cell(self.__format_value(row['id'], row['eh-dvrp cost stddev'])))
            self.write_line(r' \\')

    def write_closing_statements(self):
        self.write_line(r'\end{longtable}')
        self.write_line(r'\end{center}')


if __name__ == "__main__":
    input_path = sys.argv[1]
    output_path = sys.argv[2]

    caption = "Koszty rozwiązań poszczególnych benchmarków"
    df = pd.read_csv(input_path, delimiter='\t')
    
    with open(output_path, 'w') as f:
        tm = TableMaker(f, df)

        tm.write_opening_statements()
        tm.write_line()

        tm.write_line(r'\caption{' + caption + r'.} \\')
        tm.write_line(r'\hline')
        tm.write_header()
        tm.write_line(r'\hline')
        tm.write_line(r'\endfirsthead')
        tm.write_line()

        tm.write_line(r'\caption{' + caption + r' (kontynuacja).} \\')
        tm.write_line(r'\hline')
        tm.write_header()
        tm.write_line(r'\hline')
        tm.write_line(r'\endhead')
        tm.write_line()
        
        tm.write_line(r'\hline')
        tm.write_line(r'\endfoot')
        tm.write_line()

        tm.write_rows()

        tm.write_closing_statements()