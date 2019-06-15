package com.madao.disk.mapper;

import com.madao.disk.bean.Recycle;
import com.madao.disk.bean.RecycleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RecycleMapper {
    int countByExample(RecycleExample example);

    int deleteByExample(RecycleExample example);

    int deleteByPrimaryKey(Long recycleId);

    int insert(Recycle record);

    int insertSelective(Recycle record);

    List<Recycle> selectByExample(RecycleExample example);

    Recycle selectByPrimaryKey(Long recycleId);

    int updateByExampleSelective(@Param("record") Recycle record, @Param("example") RecycleExample example);

    int updateByExample(@Param("record") Recycle record, @Param("example") RecycleExample example);

    int updateByPrimaryKeySelective(Recycle record);

    int updateByPrimaryKey(Recycle record);
}