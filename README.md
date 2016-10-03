# NESTML - The NEST Modelling Language

NESTML is a domain specific language that supports the specification of neuron models
in a precise and concise syntax, based on the syntax of Python. Model equations
can either be given as a simple string of mathematical notation or as an algorithm written
in the built-in procedural language. The equations are analyzed by NESTML to compute
an exact solution if possible or use an appropriate numeric solver otherwise.

This is a non-official branch of the original NESTML repository dealing with the development of model-transformations from NESTML to other 
languages of the neuroscientific domain. For more information regarding the orignal work, please refer to the initial rep: https://github.com/nest/nestml