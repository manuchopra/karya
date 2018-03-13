#!/usr/bin/env python
# -*- coding: utf-8 -*- 

f = open('output.txt','w')

one = "<challenge question=\"\" type=\"2\" image=\"@drawable/fmage"

two = ".jpg\"> <answer correct=\"true\" text=\"क\"/> </challenge>"

for x in range (1, 8001):
  number = str(x)
  result = one + number + two + "\n"
  f.write(result)

f.close()

