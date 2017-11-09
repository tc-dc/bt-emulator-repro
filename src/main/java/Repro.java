import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import com.google.bigtable.admin.v2.ColumnFamily;
import com.google.bigtable.admin.v2.CreateTableRequest;
import com.google.bigtable.admin.v2.DeleteTableRequest;
import com.google.bigtable.admin.v2.Table;
import com.google.bigtable.v2.MutateRowsRequest;
import com.google.bigtable.v2.ReadRowsRequest;
import com.google.cloud.bigtable.config.BigtableOptions;
import com.google.cloud.bigtable.config.CredentialOptions;
import com.google.cloud.bigtable.grpc.BigtableSession;
import com.google.cloud.bigtable.grpc.scanner.FlatRow;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import com.google.protobuf.TextFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class Repro {
  private static Logger log = LoggerFactory.getLogger(Repro.class);
  private static final String TABLE_NAME = "itd_a6f34e35cd60454c84243e5eae84d79e";
  private static BigtableOptions btOptions = new BigtableOptions.Builder()
      .setUserAgent("repro-test")
      .setProjectId("ecnext-devel")
      .setInstanceId("integration-testing")
      .setInstanceAdminHost("127.0.0.1")
      .setDataHost("127.0.0.1")
      .setTableAdminHost("127.0.0.1")
      .setPort(8086)
      .setUsePlaintextNegotiation(true)
      .setCredentialOptions(CredentialOptions.nullCredential())
      .build();

  private static void setupTables(BigtableSession bts) throws IOException {
    try {
      bts.getTableAdminClient().deleteTable(
          DeleteTableRequest.newBuilder()
              .setName(bts.getOptions().getInstanceName().toTableNameStr(TABLE_NAME))
              .build()
      );
    } catch (StatusRuntimeException sre) {
    }

    try {
      bts.getTableAdminClient().createTable(
          CreateTableRequest.newBuilder()
              .setTableId(TABLE_NAME)
              .setParent(bts.getOptions().getInstanceName().toString())
              .setTable(Table.newBuilder()
                  .putAllColumnFamilies(
                      ImmutableMap.<String, ColumnFamily>builder()
                          .put("c", ColumnFamily.getDefaultInstance())
                          .put("s", ColumnFamily.getDefaultInstance())
                          .put("m", ColumnFamily.getDefaultInstance())
                          .put("n", ColumnFamily.getDefaultInstance())
                          .put("d", ColumnFamily.getDefaultInstance())
                          .build())
                  .build())
              .build());
    } catch (StatusRuntimeException sre) {
      if (sre.getStatus().getCode() != Status.ALREADY_EXISTS.getCode()) {
        throw new RuntimeException(sre);
      }
    }
  }

  private static MutateRowsRequest.Builder loadMutations() throws IOException {
    MutateRowsRequest.Builder mrb = MutateRowsRequest.newBuilder();

    InputStream testData = Repro.class.getResourceAsStream("test-data.pb");
    InputStreamReader isr = new InputStreamReader(testData);
    TextFormat.merge(isr, mrb);
    mrb.setTableName(btOptions.getInstanceName().toTableNameStr(TABLE_NAME));
    return mrb;
  }

  private static void readRows(BigtableSession bts) throws IOException {
    ReadRowsRequest.Builder rrb = ReadRowsRequest.newBuilder();

    InputStream testData = Repro.class.getResourceAsStream("test-request.pb");
    InputStreamReader isr = new InputStreamReader(testData);
    TextFormat.merge(isr, rrb);
    rrb.setTableName(btOptions.getInstanceName().toTableNameStr(TABLE_NAME));

    // Against Bigtable this only returns 12 rows, against the emulator it returns all of them (240)
    List<FlatRow> rows = bts.getDataClient().readFlatRowsList(rrb.build());
    log.info("Read {} rows", rows.size());
    for (FlatRow row : rows) {
      Optional<FlatRow.Cell> dim1Cell =
          row.getCells().stream()
              .filter(c -> c.getFamily().equals("d"))
              .findFirst();

      if (!dim1Cell.isPresent()) {
        log.error("Unable to find cell in results");
      } else {
        // the filter passed in should return only dim1 values of 1 or 2
        ByteString cellValueByteString = dim1Cell.get().getValue();
        byte cellValue = cellValueByteString.asReadOnlyByteBuffer().get();
        if (!(cellValue == 1 || cellValue == 2)) {
          log.error("Found row {}", row);
        }
      }
    }
  }

  public static void main(String[] args) throws IOException {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");

    BigtableSession bts = new BigtableSession(btOptions);

    setupTables(bts);

    MutateRowsRequest.Builder mrb = loadMutations();
    bts.getDataClient().mutateRows(mrb.build());

    readRows(bts);
  }
}
