package dev.krocon.shop.ep040;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/** MyBatis: das SQL steht in RevenueMapper.xml (gleicher Paketpfad unter resources). */
@Mapper
public interface RevenueMapper {

  List<StatusRevenue> revenueByStatus();
}
