package com.example.elsysandroid;

/**
 * Перечисление возможных состояний выходов контроллера
 * @author ITMO students Bazarova Anna, Denisenko Kirill, Ryabov Sergey
 * @version 1.0
 */
public enum Outs {
    /** Выключить */
    SwitchOff(0),
    /** Импульс */
    Impulse(1),
    /** Включить */
    SwitchOn(2),
    /** Переключить */
    Invert(3);
    /**
     * Поле - код состояния
     */
    private int code;

    /**
     * Функция - конструктор
     * @param code код состояния
     */
    Outs(int code){
        this.code = code;
    }

    /**
     * Функция возвращения кода состояния
     * @return возвращает код состояния
     */
    public int getCode(){
        return code;
    }
}
