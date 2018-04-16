import unittest

from astropy import units

from pynestml.codegeneration.PyNestMl2NESTTypeConverter import NESTML2NESTTypeConverter
from pynestml.symbols.BooleanTypeSymbol import BooleanTypeSymbol
from pynestml.symbols.IntegerTypeSymbol import IntegerTypeSymbol
from pynestml.symbols.NESTTimeTypeSymbol import NESTTimeTypeSymbol
from pynestml.symbols.PredefinedTypes import PredefinedTypes
from pynestml.symbols.PredefinedUnits import PredefinedUnits
from pynestml.symbols.RealTypeSymbol import RealTypeSymbol
from pynestml.symbols.StringTypeSymbol import StringTypeSymbol
from pynestml.symbols.UnitTypeSymbol import UnitTypeSymbol
from pynestml.symbols.VoidTypeSymbol import VoidTypeSymbol
from pynestml.utils.UnitType import UnitType

PredefinedUnits.register_units()
PredefinedTypes.register_types()

convert = NESTML2NESTTypeConverter.convert


class PyNestMl2NESTTypeConverterTest(unittest.TestCase):
    def test_boolean_type(self):
        bts = BooleanTypeSymbol()
        result = convert(bts)
        self.assertEqual(result, 'bool')
        return

    def test_real_type(self):
        rts = RealTypeSymbol()
        result = convert(rts)
        self.assertEqual(result, 'double')

    def test_void_type(self):
        vts = VoidTypeSymbol()
        result = convert(vts)
        self.assertEqual(result, 'void')

    def test_string_type(self):
        sts = StringTypeSymbol()
        result = convert(sts)
        self.assertEqual(result, 'std::string')

    def test_integer_type(self):
        its = IntegerTypeSymbol()
        result = convert(its)
        self.assertEqual(result, 'long')

    def test_unit_type(self):
        ms_unit = UnitType(_name=str(units.ms), _unit=units.ms)
        uts = UnitTypeSymbol(_unit=ms_unit)
        result = convert(uts)
        self.assertEqual(result, 'double')

    def test_buffer_type(self):
        bts = IntegerTypeSymbol()
        bts.is_buffer = True
        result = convert(bts)
        self.assertEqual(result, 'nest::RingBuffer')

    def test_time_type(self):
        tts = NESTTimeTypeSymbol()
        result = convert(tts)
        self.assertEqual(result, 'nest::Time')