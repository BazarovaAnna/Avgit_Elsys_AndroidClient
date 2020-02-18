package com.example.elsysandroid;

/**
 * Перечисление возможных состояний выходов контроллера
 * @author ITMO students Bazarova Anna, Denisenko Kirill, Ryabov Sergey
 * @version 1.0
 */
public enum Outs {
    /** Выключить  */
    SwitchOff(DevType.Out,0),
    /** Импульс */
    Impulse(DevType.Out,1),
    /** Включить */
    SwitchOn(DevType.Out,2),
    /** Переключить */
    Invert(DevType.Out,3),

    None(DevType.None,10000);
    /**
     * Поле - код состояния
     */
    private int code;
    /**
     * Поле - устройство
     */
    private DevType dev;
    /**
     * Функция - конструктор
     * @param code код состояния
     */
    Outs(DevType dev, int code){
        this.code = code;
        this.dev = dev;
    }

    /**
     * Функция возвращения кода состояния
     * @return возвращает код состояния
     */
    public int getCode(){
        return code;
    }
    /**
     * Функция возвращения устройства
     * @return возвращает устройство
     */
    public DevType getDevType(){
        return dev;
    }
}
