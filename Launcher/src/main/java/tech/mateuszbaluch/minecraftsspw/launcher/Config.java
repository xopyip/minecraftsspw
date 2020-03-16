package tech.mateuszbaluch.minecraftsspw.launcher;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.FileWriter;
import java.io.IOException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Config {
    private String nickname;
    private int ram;

    public void save(){
        Gson gson = new Gson();
        try(FileWriter fileWriter = new FileWriter(Main.CONFIG_FILE)){
            fileWriter.write(gson.toJson(this));
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
