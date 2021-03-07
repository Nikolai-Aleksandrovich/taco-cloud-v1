delete from Taco_Order_Tacos;
delete from Taco_Ingredients;
delete from Taco;
delete from Taco_Order;

delete from Ingredient;
insert into ingredient(id,name,type) values("FLTO", "Flour Tortilla", Type.WRAP)
insert into ingredient(id,name,type) values("COTO", "Corn Tortilla", Type.WRAP)
insert into ingredient(id,name,type) values("GRBF", "Ground Beef", Type.PROTEIN)
insert into ingredient(id,name,type) values("CARN", "Carnitas", Type.PROTEIN)
insert into ingredient(id,name,type) values("TMTO", "Diced Tomatoes", Type.VEGGIES)
insert into ingredient(id,name,type) values("LETC", "Lettuce", Type.VEGGIES)
insert into ingredient(id,name,type) values("CHED", "Cheddar", Type.CHEESE)
insert into ingredient(id,name,type) values("JACK", "Monterrey Jack", Type.CHEESE)
insert into ingredient(id,name,type) values("SLSA", "Salsa", Type.SAUCE)
insert into ingredient(id,name,type) values("SRCR", "Sour Cream", Type.SAUCE)

