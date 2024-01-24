import numpy as np
import sys

if __name__ == '__main__':
    a = np.ones(3)
    print(a)
    print('恭喜您！java调用python代码成功')

    print('脚本名为：%s'%(sys.argv[0]))
    print('传入的参数为：')
    for i in range(1, len(sys.argv)):
        print('参数:%s'%(sys.argv[i]))