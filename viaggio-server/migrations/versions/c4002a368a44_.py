"""empty message

Revision ID: c4002a368a44
Revises: 727dff2022a5
Create Date: 2019-05-24 16:11:51.165169

"""
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import mysql

# revision identifiers, used by Alembic.
revision = 'c4002a368a44'
down_revision = '727dff2022a5'
branch_labels = None
depends_on = None


def upgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.alter_column('travelcards', 'isDelete',
               existing_type=mysql.TINYINT(display_width=1),
               type_=sa.Boolean(),
               existing_nullable=True)
    op.alter_column('travelcards', 'travelLocalId',
               existing_type=mysql.INTEGER(display_width=11),
               type_=sa.BigInteger(),
               existing_nullable=False)
    op.drop_constraint('travelcards_ibfk_1', 'travelcards', type_='foreignkey')
    op.create_foreign_key(None, 'travelcards', 'travels', ['travelId'], ['id'])
    op.alter_column('travels', 'isDelete',
               existing_type=mysql.TINYINT(display_width=1),
               type_=sa.Boolean(),
               existing_nullable=True)
    op.alter_column('travels', 'share',
               existing_type=mysql.TINYINT(display_width=1),
               type_=sa.Boolean(),
               existing_nullable=True)
    op.drop_constraint('travels_ibfk_1', 'travels', type_='foreignkey')
    op.create_foreign_key(None, 'travels', 'users', ['userId'], ['id'])
    # ### end Alembic commands ###


def downgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.drop_constraint(None, 'travels', type_='foreignkey')
    op.create_foreign_key('travels_ibfk_1', 'travels', 'users', ['userId'], ['id'], ondelete='CASCADE')
    op.alter_column('travels', 'share',
               existing_type=sa.Boolean(),
               type_=mysql.TINYINT(display_width=1),
               existing_nullable=True)
    op.alter_column('travels', 'isDelete',
               existing_type=sa.Boolean(),
               type_=mysql.TINYINT(display_width=1),
               existing_nullable=True)
    op.drop_constraint(None, 'travelcards', type_='foreignkey')
    op.create_foreign_key('travelcards_ibfk_1', 'travelcards', 'travels', ['travelId'], ['id'], ondelete='CASCADE')
    op.alter_column('travelcards', 'travelLocalId',
               existing_type=sa.BigInteger(),
               type_=mysql.INTEGER(display_width=11),
               existing_nullable=False)
    op.alter_column('travelcards', 'isDelete',
               existing_type=sa.Boolean(),
               type_=mysql.TINYINT(display_width=1),
               existing_nullable=True)
    # ### end Alembic commands ###