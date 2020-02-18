package com.example.elsysandroid;
/**
 * Перечисление типов девайсов
 *
 * @author ITMO students Bazarova Anna, Denisenko Kirill, Ryabov Sergey, Chernyshev Nikita
 * @version 1.0
 */
public enum DevType {
    /** Выходы*/
    Out(10),
    //Door(3),
    //Turn(22),
    //Gate(4),
    //Zone(12),
    //Part(13),
    //CU(5),
    //Reader(19),
    //PartGroup(27),
    //MBNet(28),
    //NetGroup(40),
    /** Нет устройства*/
    None(10000);
    /**
     * Поле - код состояния
     */
    private int code;
    /**
     * Функция - конструктор
     * @param code код состояния
     */
    DevType(int code){
        this.code=code;
    }
    /**
     * Функция возвращения кода состояния
     * @return возвращает код состояния
     */
    public int getCode(){
        return code;
    }
}
