package cn.cetasas.db.util;

import cn.cetasas.db.mapper.*;
import cn.cetasas.db.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.util.Collection;

public class InsertBatch {
    private static final Logger LOG = LoggerFactory.getLogger(InsertBatch.class);

    private final CESMapper cesMapper;

    private final EESMapper eesMapper;

    private final TotalMapper totalMapper;

    private final RCodeMapper rCodeMapper;

    private final ECodeMapper eCodeMapper;

    public InsertBatch(CESMapper cesMapper, EESMapper eesMapper, TotalMapper totalMapper, RCodeMapper rCodeMapper, ECodeMapper eCodeMapper) {
        this.cesMapper = cesMapper;
        this.eesMapper = eesMapper;
        this.totalMapper = totalMapper;
        this.rCodeMapper = rCodeMapper;
        this.eCodeMapper = eCodeMapper;
    }

    @Async
    public void insertBatch_CES(Collection<CES> ces, String year) {
        LOG.info("【{}】异步处理", Thread.currentThread().getName());
        long start = System.currentTimeMillis();
        ces.forEach(c -> cesMapper.insert(year + TableSuffix.CES, c));
        long end = System.currentTimeMillis();
        LOG.info("共耗时：{} ms", end - start);
    }

//    @Async // 由于 EES 是 CES 的基础表，而在本项目中对于 EES 的获取采用23年以前的数据懒加载，23年及23年之后的数据在每年发布日定时任务爬虫获取，所以该表不能采取异步的形式获取
    public void insertBatch_EES(Collection<EES> ees, String year) {
        LOG.info("【{}】正在处理", Thread.currentThread().getName());
        long start = System.currentTimeMillis();
        ees.forEach(e -> eesMapper.insert(year + TableSuffix.EES, e));
        long end = System.currentTimeMillis();
        LOG.info("共耗时：{} ms", end - start);
    }

//    @Async // 该业务暂时还未用到
    public void insertBatch_Total(Collection<Total> total, String year) {
        LOG.info("【{}】正在处理", Thread.currentThread().getName());
        long start = System.currentTimeMillis();
        total.forEach(t -> totalMapper.insert(year + TableSuffix.TOTAL, t));
        long end = System.currentTimeMillis();
        LOG.info("共耗时：{} ms", end - start);
    }

//    @Async // 由于 r_code 表是其他表的外键表，因此不能异步处理，其他表的创建必须在其之后
    public void insertBatch_RCode(Collection<RCode> rCodes) {
        LOG.info("【{}】正在处理", Thread.currentThread().getName());
        long start = System.currentTimeMillis();
        rCodes.forEach(rCodeMapper::insert);
        long end = System.currentTimeMillis();
        LOG.info("共耗时：{} ms", end - start);
    }

//    @Async // 由于 e_code 表是其他表的外键表，因此不能异步处理，其他表的创建必须在其之后
    public void insertBatch_ECode(Collection<ECode> eCodes) {
        LOG.info("【{}】正在处理", Thread.currentThread().getName());
        long start = System.currentTimeMillis();
        eCodes.forEach(eCodeMapper::insert);
        long end = System.currentTimeMillis();
        LOG.info("共耗时：{} ms", end - start);
    }

    // 和 @Transactional 冲突
//    private final SqlSessionFactory sqlSessionFactory;
//
//    public InsertBatch(SqlSessionFactory sqlSessionFactory) {
//        this.sqlSessionFactory = sqlSessionFactory;
//    }
//
//    @Async
//    public void insertBatch_CES(Collection<CES> ces, String year) {
//        LOG.info("【{}】异步处理", Thread.currentThread().getName());
//        long start = System.currentTimeMillis();
//        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
//            CESMapper mapper = sqlSession.getMapper(CESMapper.class);
//            ces.forEach(c -> mapper.insert(year + TableSuffix.CES, c));
//            sqlSession.commit();
//            sqlSession.clearCache();
//        } finally {
//            long end = System.currentTimeMillis();
//            LOG.info("共耗时：{} ms", end - start);
//        }
//    }
//
//    @Async
//    public void insertBatch_EES(Collection<EES> ees, String year) {
//        LOG.info("【{}】异步处理", Thread.currentThread().getName());
//        long start = System.currentTimeMillis();
//        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
//            EESMapper mapper = sqlSession.getMapper(EESMapper.class);
//            ees.forEach(e -> mapper.insert(year + TableSuffix.EES, e));
//            sqlSession.commit();
//            sqlSession.clearCache();
//        } finally {
//            long end = System.currentTimeMillis();
//            LOG.info("共耗时：{} ms", end - start);
//        }
//    }
//
//    @Async
//    public void insertBatch_Total(Collection<Total> total, String year) {
//        LOG.info("【{}】异步处理", Thread.currentThread().getName());
//        long start = System.currentTimeMillis();
//        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
//            TotalMapper mapper = sqlSession.getMapper(TotalMapper.class);
//            total.forEach(t -> mapper.insert(year + TableSuffix.TOTAL, t));
//            sqlSession.commit();
//            sqlSession.clearCache();
//        } finally {
//            long end = System.currentTimeMillis();
//            LOG.info("共耗时：{} ms", end - start);
//        }
//    }
//
////    @Async // 由于 r_code 表是其他表的外键表，因此不能异步处理，其他表的创建必须在其之后
//    public void insertBatch_RegCode(Collection<RCode> regCodes) {
//        LOG.info("【{}】正在处理", Thread.currentThread().getName());
//        long start = System.currentTimeMillis();
//        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
//            RCodeMapper mapper = sqlSession.getMapper(RCodeMapper.class);
//            regCodes.forEach(mapper::insert);
//            sqlSession.commit();
//            sqlSession.clearCache();
//        } finally {
//            long end = System.currentTimeMillis();
//            LOG.info("共耗时：{} ms", end - start);
//        }
//    }
//
////    @Async // 由于 e_code 表是其他表的外键表，因此不能异步处理，其他表的创建必须在其之后
//    public void insertBatch_EnergyCode(Collection<ECode> energyCodes) {
//        LOG.info("【{}】正在处理", Thread.currentThread().getName());
//        long start = System.currentTimeMillis();
//        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
//            ECodeMapper mapper = sqlSession.getMapper(ECodeMapper.class);
//            energyCodes.forEach(mapper::insert);
//            sqlSession.commit();
//            sqlSession.clearCache();
//        } finally {
//            long end = System.currentTimeMillis();
//            LOG.info("共耗时：{} ms", end - start);
//        }
//    }
}
