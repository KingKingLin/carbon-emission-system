#引包
import requests
import time
import sys
#生成时间戳
def getTime():
    return int(round(time.time() * 1000))
#爬虫代码
if __name__ == '__main__':
#     print('脚本名为：%s'%(sys.argv[0]))
#     print('传入的参数为：')
#     for i in range(1, len(sys.argv)):
#         print('参数:%s'%(sys.argv[i]))
    url='https://data.stats.gov.cn/easyquery.htm'
    headers={'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.69'}
    key={}#参数键值对
    key['m']='getOtherWds'
    key['dbcode']='fsnd'
    key['rowcode']='zb'
    key['colcode']='sj'
    key['wds']='[{"wdcode":"zb","valuecode":"%s"}]'%(sys.argv[1])
    key['k1']=str(getTime())
    requests.packages.urllib3.disable_warnings()
    r=requests.get(url,headers=headers,params=key,verify=False)
    print(r.json())