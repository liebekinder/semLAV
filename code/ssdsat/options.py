import sys
import logging

# path of the c2d compiler
c2d = "/home/gabriela/gun2012/code/ssdsat/c2d/c2d_linux"

# path of the model enumerators
models ="/home/gabriela/gun2012/code/ssdsat/models/models"
bestmodel = "/home/gabriela/gun2012/code/ssdsat/models/bestmodel"

# maximum number of models to be displayed
max_models = 500

# logging level and output file
loglevel = logging.DEBUG
logging_output_file = None

# user defined options
user_options = {}
